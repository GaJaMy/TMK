(function () {
    const BASE_URL = "http://localhost:8080";
    const storageKeys = {
        accessToken: "tmk-ui-access-token",
        refreshToken: "tmk-ui-refresh-token",
        lastEmail: "tmk-ui-last-email"
    };

    const state = {
        currentExam: null,
        activeQuestionOrder: null,
        logs: [],
        documentStreams: {}
    };

    function byId(id) {
        return document.getElementById(id);
    }

    function token() {
        return localStorage.getItem(storageKeys.accessToken) || "";
    }

    function refreshTokenValue() {
        return localStorage.getItem(storageKeys.refreshToken) || "";
    }

    function lastEmail() {
        return localStorage.getItem(storageKeys.lastEmail) || "";
    }

    function isLoggedIn() {
        return !!token();
    }

    function saveTokens(accessToken, refreshToken) {
        if (accessToken !== undefined) localStorage.setItem(storageKeys.accessToken, accessToken || "");
        if (refreshToken !== undefined) localStorage.setItem(storageKeys.refreshToken, refreshToken || "");
        renderAuthState();
    }

    function clearTokens() {
        localStorage.removeItem(storageKeys.accessToken);
        localStorage.removeItem(storageKeys.refreshToken);
        renderAuthState();
    }

    function saveEmail(email) {
        if (email) localStorage.setItem(storageKeys.lastEmail, email);
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    function formatJson(value) {
        return JSON.stringify(value, null, 2);
    }

    function setText(id, value) {
        const node = byId(id);
        if (node) node.textContent = value;
    }

    function setNotice(id, message) {
        const node = byId(id);
        if (node) node.textContent = message;
    }

    function closeDocumentStream(scopeKey) {
        const existing = state.documentStreams[scopeKey];
        if (existing) {
            existing.close();
            delete state.documentStreams[scopeKey];
        }
    }

    function subscribeDocumentEvents(scopeKey, documentId, isAdmin) {
        closeDocumentStream(scopeKey);

        const path = isAdmin
            ? "/admin/v1/documents/" + documentId + "/events"
            : "/api/v1/my/documents/" + documentId + "/events";
        const eventSource = new EventSource(BASE_URL + path + "?accessToken=" + encodeURIComponent(token()));
        state.documentStreams[scopeKey] = eventSource;

        const noticeId = isAdmin ? "adminStreamNotice" : "documentStreamNotice";
        setNotice(noticeId, "문서 처리 이벤트를 구독 중입니다. 완료 시 자동 반영됩니다.");

        function handlePayload(payload) {
            if (isAdmin) {
                renderAdminResult("문서 처리 상태", payload);
            } else {
                renderDocumentSummary(payload);
            }

            if (payload.status === "COMPLETED") {
                setNotice(noticeId, "문서 등록 및 문제 생성이 완료되었습니다.");
                closeDocumentStream(scopeKey);
            } else if (payload.status === "FAILED") {
                setNotice(noticeId, "문서 처리에 실패했습니다. 응답 상세와 서버 로그를 확인하세요.");
                closeDocumentStream(scopeKey);
            } else {
                setNotice(noticeId, "문서 처리 중입니다. 현재 상태: " + payload.status);
            }
        }

        eventSource.addEventListener("connected", function (event) {
            const payload = JSON.parse(event.data);
            handlePayload(payload);
        });

        eventSource.addEventListener("document-status", function (event) {
            const payload = JSON.parse(event.data);
            handlePayload(payload);
        });

        eventSource.onerror = function () {
            setNotice(noticeId, "이벤트 연결이 종료되었거나 실패했습니다. 필요하면 상태 조회를 다시 실행하세요.");
            closeDocumentStream(scopeKey);
        };
    }

    function renderResponse(title, payload) {
        const view = byId("responseView");
        if (!view) return;
        view.textContent = title + "\n\n" + formatJson(payload);
    }

    function compactPayload(payload) {
        const json = formatJson(payload);
        return json.length > 180 ? json.slice(0, 180) + "..." : json;
    }

    function addLog(method, path, status, payload) {
        state.logs.unshift({
            method: method,
            path: path,
            status: status,
            payload: payload,
            timestamp: new Date().toLocaleString("ko-KR")
        });
        state.logs = state.logs.slice(0, 12);
        renderLogs();
    }

    function renderLogs() {
        const container = byId("requestLogs");
        if (!container) return;

        if (!state.logs.length) {
            container.innerHTML = '<div class="empty">아직 호출된 API가 없습니다.</div>';
            return;
        }

        container.innerHTML = state.logs.map(function (entry) {
            return [
                '<div class="log-entry">',
                "<strong>" + escapeHtml(entry.method + " " + entry.path) + " [" + escapeHtml(String(entry.status)) + "]</strong>",
                "<div>" + escapeHtml(entry.timestamp) + "</div>",
                "<div class=\"dimmed\">" + escapeHtml(compactPayload(entry.payload)) + "</div>",
                "</div>"
            ].join("");
        }).join("");
    }

    async function callApi(path, options) {
        const requestOptions = {
            method: options.method || "GET",
            headers: options.headers ? { ...options.headers } : {}
        };
        const isMultipart = options.body instanceof FormData;

        if (!isMultipart && !requestOptions.headers["Content-Type"]) {
            requestOptions.headers["Content-Type"] = "application/json";
        }

        if (options.auth && token()) {
            requestOptions.headers.Authorization = "Bearer " + token();
        }

        if (options.body !== undefined) {
            requestOptions.body = isMultipart ? options.body : JSON.stringify(options.body);
        }

        const response = await fetch(path, requestOptions);
        const text = await response.text();
        let data;

        try {
            data = text ? JSON.parse(text) : null;
        } catch (error) {
            data = { raw: text };
        }

        renderResponse(requestOptions.method + " " + BASE_URL + path + " [" + response.status + "]", data);
        addLog(requestOptions.method, path, response.status, data);

        if (!response.ok) {
            throw new Error((data && data.msg) || ("HTTP " + response.status));
        }

        return data;
    }

    function bindButton(id, handler) {
        const button = byId(id);
        if (!button) return;
        button.addEventListener("click", async function () {
            try {
                await handler();
            } catch (error) {
                console.error(error);
            }
        });
    }

    function bindForm(id, handler) {
        const form = byId(id);
        if (!form) return;
        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            try {
                await handler(event.target);
            } catch (error) {
                console.error(error);
            }
        });
    }

    function formJson(form) {
        const data = new FormData(form);
        const result = {};
        data.forEach(function (value, key) {
            if (value !== "") result[key] = value;
        });
        return result;
    }

    function renderAuthState() {
        document.querySelectorAll("[data-auth-summary]").forEach(function (node) {
            node.textContent = isLoggedIn() ? "로그인됨" : "비로그인";
        });
        document.querySelectorAll("[data-user-email]").forEach(function (node) {
            node.textContent = lastEmail() || "저장된 사용자 없음";
        });

        const accessTokenField = byId("accessToken");
        const refreshTokenField = byId("refreshToken");
        if (accessTokenField) accessTokenField.value = token();
        if (refreshTokenField) refreshTokenField.value = refreshTokenValue();
    }

    function requireAuth() {
        if (!isLoggedIn()) {
            location.href = "/login.html";
            return false;
        }
        return true;
    }

    function metaRow(label, value) {
        return '<div class="meta-row"><span>' + escapeHtml(label) + '</span><strong>' + escapeHtml(String(value)) + '</strong></div>';
    }

    function setupTabs() {
        document.querySelectorAll("[data-tab]").forEach(function (button) {
            button.addEventListener("click", function () {
                document.querySelectorAll("[data-tab]").forEach(function (item) {
                    item.classList.remove("active");
                });
                document.querySelectorAll("[data-tab-panel]").forEach(function (panel) {
                    panel.classList.remove("active");
                });
                button.classList.add("active");
                const panel = byId(button.dataset.tab);
                if (panel) panel.classList.add("active");
            });
        });
    }

    function renderHomeState() {
        renderDashboardState();
    }

    function renderDashboardState() {
        setText("dashboardAuthState", isLoggedIn() ? "Connected" : "Offline");
        setText("dashboardTokenState", token() ? "Stored" : "None");
    }

    function renderAdminResult(title, result) {
        const target = byId("adminResult");
        if (!target) return;

        if (!result) {
            target.innerHTML = '<div class="empty">관리자 작업 결과가 여기에 표시됩니다.</div>';
            return;
        }

        target.innerHTML = [
            '<article class="question-card">',
            "<h3>" + escapeHtml(title) + "</h3>",
            '<div class="meta-list">',
            Object.keys(result).map(function (key) {
                const value = typeof result[key] === "object" ? formatJson(result[key]) : result[key];
                return metaRow(key, value == null ? "-" : value);
            }).join(""),
            "</div>",
            "</article>"
        ].join("");
    }

    function renderDocumentSummary(result) {
        const target = byId("documentSummary");
        if (!target) return;

        if (!result) {
            target.innerHTML = '<div class="empty">문서 등록 또는 상태 조회 결과가 여기에 표시됩니다.</div>';
            return;
        }

        target.innerHTML = [
            '<div class="status-card">',
            "<h3>" + escapeHtml(result.title || "문서") + "</h3>",
            '<div class="meta-list">',
            metaRow("문서 ID", result.documentId),
            metaRow("주제", result.topic || "-"),
            metaRow("범위", result.scope || "-"),
            metaRow("상태", result.status),
            metaRow("청크 수", result.chunkCount != null ? result.chunkCount : "-"),
            metaRow("문항 수", result.questionCount != null ? result.questionCount : "-"),
            metaRow("생성 시각", result.createdAt || "-"),
            "</div>",
            "</div>"
        ].join("");
    }

    function renderQuestionList(result) {
        const target = byId("questionList");
        if (!target) return;

        if (!result || !result.content || !result.content.length) {
            target.innerHTML = '<div class="empty">조회된 문제가 없습니다.</div>';
            return;
        }

        target.innerHTML = result.content.map(function (question) {
            return [
                '<article class="question-card">',
                "<div class=\"inline-actions\"><span class=\"badge\">#" + escapeHtml(String(question.questionId)) + "</span><span class=\"tag\">" + escapeHtml(question.type) + "</span><span class=\"tag\">" + escapeHtml(question.difficulty) + "</span><span class=\"tag\">" + escapeHtml(question.topic || "-") + "</span><span class=\"tag\">" + escapeHtml(question.scope || "-") + "</span><span class=\"tag\">" + escapeHtml(question.sourceType || "-") + "</span></div>",
                "<h3>" + escapeHtml(question.content) + "</h3>",
                '<div class="button-row"><button type="button" class="secondary" data-question-id="' + escapeHtml(String(question.questionId)) + '">상세 보기</button></div>',
                "</article>"
            ].join("");
        }).join("");

        target.querySelectorAll("[data-question-id]").forEach(function (button) {
            button.addEventListener("click", function () {
                loadQuestionDetail(button.dataset.questionId);
            });
        });

        setText("questionMeta", "총 " + result.totalElements + "건 / " + (result.page + 1) + "페이지");
    }

    function renderQuestionDetail(result) {
        const target = byId("questionDetail");
        if (!target) return;

        if (!result) {
            target.innerHTML = '<div class="empty">문제를 선택하면 상세 내용이 표시됩니다.</div>';
            return;
        }

        const options = (result.options || []).map(function (option) {
            return '<div class="option-item">' + escapeHtml(option.number + ". " + option.content) + "</div>";
        }).join("");

        target.innerHTML = [
            '<article class="question-card">',
            "<div class=\"inline-actions\"><span class=\"badge\">#" + escapeHtml(String(result.questionId)) + "</span><span class=\"tag\">" + escapeHtml(result.type) + "</span><span class=\"tag\">" + escapeHtml(result.difficulty) + "</span><span class=\"tag\">" + escapeHtml(result.topic || "-") + "</span><span class=\"tag\">" + escapeHtml(result.scope || "-") + "</span><span class=\"tag\">" + escapeHtml(result.sourceType || "-") + "</span></div>",
            "<h3>" + escapeHtml(result.content) + "</h3>",
            '<div class="option-list">' + (options || '<div class="empty">선택지 없음</div>') + "</div>",
            '<div class="meta-list">',
            metaRow("정답", result.answer || "-"),
            metaRow("해설", result.explanation || "-"),
            "</div>",
            "</article>"
        ].join("");
    }

    function renderExamOverview(result) {
        const target = byId("examOverview");
        if (!target) return;

        if (!result) {
            target.innerHTML = '<div class="empty">시험을 시작하면 개요가 표시됩니다.</div>';
            return;
        }

        target.innerHTML = [
            '<div class="status-card">',
            "<h3>시험 #" + escapeHtml(String(result.examId)) + "</h3>",
            '<div class="meta-list">',
            metaRow("상태", result.status || "-"),
            metaRow("문항 수", result.totalQuestions != null ? result.totalQuestions : (result.questions ? result.questions.length : "-")),
            metaRow("제한 시간", result.timeLimit != null ? result.timeLimit + "분" : "-"),
            metaRow("시작 시각", result.startedAt || "-"),
            metaRow("종료 시각", result.expiredAt || "-"),
            "</div>",
            "</div>"
        ].join("");
    }

    function renderQuestionNavigation(questions) {
        const target = byId("examQuestionNav");
        if (!target) return;

        if (!questions || !questions.length) {
            target.innerHTML = '<div class="empty">시험을 불러오면 문항 번호가 표시됩니다.</div>';
            return;
        }

        target.innerHTML = questions.map(function (question) {
            const hasAnswer = !!String(question.myAnswer || "").trim();
            const activeClass = state.activeQuestionOrder === question.order ? " active" : "";
            const answeredClass = hasAnswer ? " answered" : "";
            return '<button type="button" class="nav-chip' + answeredClass + activeClass + '" data-jump-order="' + escapeHtml(String(question.order)) + '">' + escapeHtml(String(question.order)) + "</button>";
        }).join("");

        target.querySelectorAll("[data-jump-order]").forEach(function (button) {
            button.addEventListener("click", function () {
                const order = button.dataset.jumpOrder;
                const card = document.querySelector('[data-question-order="' + order + '"]');
                if (card) {
                    state.activeQuestionOrder = Number(order);
                    renderQuestionNavigation(state.currentExam ? state.currentExam.questions : []);
                    card.scrollIntoView({ behavior: "smooth", block: "start" });
                }
            });
        });
    }

    function renderExamQuestions(result) {
        const target = byId("examQuestions");
        if (!target) return;

        if (!result || !result.questions || !result.questions.length) {
            target.innerHTML = '<div class="empty">시험을 시작하거나 불러오면 문제와 보기가 표시됩니다.</div>';
            renderQuestionNavigation([]);
            setText("answeredCount", "0");
            return;
        }

        if (!state.activeQuestionOrder) {
            state.activeQuestionOrder = result.questions[0].order;
        }

        target.innerHTML = result.questions.map(function (question) {
            const options = (question.options || []).length ? (question.options || []).map(function (option) {
                const checked = String(question.myAnswer || "") === String(option.number) ? " checked" : "";
                return [
                    '<label class="choice-item">',
                    '<input type="radio" name="answer-' + escapeHtml(String(question.questionId)) + '" value="' + escapeHtml(String(option.number)) + '" data-answer-question-id="' + escapeHtml(String(question.questionId)) + '"' + checked + '>',
                    '<span class="choice-label"><span class="choice-number">' + escapeHtml(String(option.number)) + '</span><span class="choice-text">' + escapeHtml(option.content) + "</span></span>",
                    "</label>"
                ].join("");
            }).join("") : [
                '<label class="answer-input">답안',
                '<textarea class="exam-answer-area" rows="' + (question.type === "SHORT_ANSWER" ? "5" : "8") + '" data-answer-question-id="' + escapeHtml(String(question.questionId)) + '" placeholder="긴 답안도 입력할 수 있습니다.">' + escapeHtml(question.myAnswer || "") + '</textarea>',
                '</label>'
            ].join("");

            return [
                '<article class="exam-question-card" data-question-order="' + escapeHtml(String(question.order)) + '">',
                '<div class="exam-question-head">',
                '<div class="inline-actions"><span class="badge">' + escapeHtml(String(question.order)) + '번</span><span class="tag">' + escapeHtml(question.type) + '</span><span class="tag">' + escapeHtml(question.difficulty) + '</span><span class="tag">' + escapeHtml(question.topic || "-") + '</span><span class="tag">' + escapeHtml(question.scope || "-") + '</span></div>',
                '<span class="status-inline" data-answer-status="' + escapeHtml(String(question.questionId)) + '">' + escapeHtml(String(question.myAnswer || "").trim() ? "답안 입력됨" : "미응답") + "</span>",
                "</div>",
                '<h2 class="exam-question-title">' + escapeHtml(question.content) + "</h2>",
                '<div class="choice-list">' + options + "</div>",
                "</article>"
            ].join("");
        }).join("");

        attachExamAnswerHandlers();
        renderQuestionNavigation(result.questions);
        updateAnsweredCount();
    }

    function attachExamAnswerHandlers() {
        document.querySelectorAll("[data-answer-question-id]").forEach(function (input) {
            const eventName = input.tagName === "TEXTAREA" || input.type === "text" ? "input" : "change";
            input.addEventListener(eventName, function () {
                syncAnswersToState();
                updateAnsweredCount();
                renderQuestionNavigation(state.currentExam ? state.currentExam.questions : []);
                refreshAnswerStatus();
            });
        });
    }

    function refreshAnswerStatus() {
        if (!state.currentExam || !state.currentExam.questions) return;
        state.currentExam.questions.forEach(function (question) {
            const node = document.querySelector('[data-answer-status="' + question.questionId + '"]');
            if (node) {
                node.textContent = String(question.myAnswer || "").trim() ? "답안 입력됨" : "미응답";
            }
        });
    }

    function syncAnswersToState() {
        if (!state.currentExam || !state.currentExam.questions) return;

        state.currentExam.questions.forEach(function (question) {
            const radio = document.querySelector('input[data-answer-question-id="' + question.questionId + '"]:checked');
            const textInput = document.querySelector('[data-answer-question-id="' + question.questionId + '"]:not([type="radio"])');
            if (radio) {
                question.myAnswer = radio.value;
            } else if (textInput) {
                question.myAnswer = textInput.value;
            }
        });
    }

    function updateAnsweredCount() {
        const answers = collectAnswers();
        const answered = answers.filter(function (item) {
            return String(item.answer || "").trim() !== "";
        }).length;
        setText("answeredCount", String(answered));
    }

    function collectAnswers() {
        if (!state.currentExam || !state.currentExam.questions) return [];

        return state.currentExam.questions.map(function (question) {
            const checked = document.querySelector('input[data-answer-question-id="' + question.questionId + '"]:checked');
            const textInput = document.querySelector('[data-answer-question-id="' + question.questionId + '"]:not([type="radio"])');
            return {
                questionId: Number(question.questionId),
                answer: checked ? checked.value : (textInput ? textInput.value : "")
            };
        });
    }

    function renderExamResult(result) {
        const target = byId("examResultPanel");
        if (!target) return;

        if (!result) {
            target.innerHTML = '<div class="empty">제출하거나 결과 조회를 실행하면 점수가 표시됩니다.</div>';
            return;
        }

        target.innerHTML = [
            '<div class="metric-card">',
            "<h3>시험 결과</h3>",
            '<div class="metric-value">' + escapeHtml(String(result.score)) + "</div>",
            '<div class="meta-list">',
            metaRow("시험 ID", result.examId),
            metaRow("정답 수", result.correctCount + " / " + result.totalQuestions),
            metaRow("합격", result.passed ? "PASS" : "FAIL"),
            metaRow("제출 시각", result.submittedAt || "-"),
            "</div>",
            "</div>"
        ].join("");
    }

    function renderHistoryList(result) {
        const target = byId("examHistoryList");
        if (!target) return;

        if (!result || !result.content || !result.content.length) {
            target.innerHTML = '<div class="empty">시험 이력이 없습니다.</div>';
            return;
        }

        target.innerHTML = result.content.map(function (history) {
            return [
                '<article class="history-card">',
                "<h3>시험 #" + escapeHtml(String(history.examId)) + "</h3>",
                '<div class="meta-list">',
                metaRow("점수", history.score),
                metaRow("정답 수", history.correctCount + " / " + history.totalQuestions),
                metaRow("합격", history.passed ? "PASS" : "FAIL"),
                metaRow("제출 시각", history.submittedAt || "-"),
                "</div>",
                '<div class="button-row"><button type="button" class="secondary" data-history-id="' + escapeHtml(String(history.examId)) + '">상세 보기</button></div>',
                "</article>"
            ].join("");
        }).join("");

        target.querySelectorAll("[data-history-id]").forEach(function (button) {
            button.addEventListener("click", function () {
                loadHistoryDetail(button.dataset.historyId);
            });
        });
    }

    function renderHistoryDetail(result) {
        const target = byId("examHistoryDetail");
        if (!target) return;

        if (!result) {
            target.innerHTML = '<div class="empty">이력 상세를 선택하면 문제별 채점 결과가 표시됩니다.</div>';
            return;
        }

        const questions = (result.questions || []).map(function (question) {
            const options = (question.options || []).map(function (option) {
                return '<div class="option-item">' + escapeHtml(option.number + ". " + option.content) + "</div>";
            }).join("");

            return [
                '<article class="question-card ' + (question.isCorrect ? "correct" : "wrong") + '">',
                "<div class=\"inline-actions\"><span class=\"badge\">" + escapeHtml(String(question.order)) + "번</span><span class=\"tag\">" + escapeHtml(question.type) + "</span><span class=\"tag\">" + escapeHtml(question.difficulty) + "</span></div>",
                "<h3>" + escapeHtml(question.content) + "</h3>",
                '<div class="option-list">' + options + "</div>",
                '<div class="meta-list">',
                metaRow("내 답안", question.myAnswer || "-"),
                metaRow("정답", question.answer || "-"),
                metaRow("정오", question.isCorrect ? "정답" : "오답"),
                metaRow("해설", question.explanation || "-"),
                "</div>",
                "</article>"
            ].join("");
        }).join("");

        target.innerHTML = [
            '<div class="status-card">',
            "<h3>시험 #" + escapeHtml(String(result.examId)) + " 상세</h3>",
            '<div class="meta-list">',
            metaRow("점수", result.score),
            metaRow("정답 수", result.correctCount + " / " + result.totalQuestions),
            metaRow("합격", result.passed ? "PASS" : "FAIL"),
            metaRow("제출 시각", result.submittedAt || "-"),
            "</div>",
            "</div>",
            '<div class="list">' + questions + "</div>"
        ].join("");
    }

    async function loadQuestionDetail(questionId) {
        const response = await callApi("/api/v1/questions/" + questionId, { method: "GET", auth: true });
        renderQuestionDetail(response.data);
    }

    async function loadExam(examId) {
        const response = await callApi("/api/v1/exams/" + examId, { method: "GET", auth: true });
        state.currentExam = response.data;
        state.activeQuestionOrder = response.data.questions && response.data.questions.length ? response.data.questions[0].order : null;
        renderExamOverview(response.data);
        renderExamQuestions(response.data);
        setText("activeExamId", String(response.data.examId));
        return response.data;
    }

    async function loadHistoryDetail(examId) {
        const response = await callApi("/api/v1/exams/history/" + examId, { method: "GET", auth: true });
        renderHistoryDetail(response.data);
    }

    function initCommon() {
        renderAuthState();
        renderLogs();

        bindButton("clearResponseBtn", function () {
            renderResponse("응답 로그", { msg: "화면 로그가 초기화되었습니다." });
        });

        bindButton("clearTokensBtn", function () {
            clearTokens();
            addLog("LOCAL", "token-storage", 200, { msg: "저장된 토큰을 삭제했습니다." });
        });

        bindButton("logoutBtn", async function () {
            if (token()) {
                try {
                    await callApi("/api/v1/auth/logout", { method: "POST", auth: true });
                } catch (error) {
                    console.error(error);
                }
            }
            clearTokens();
            location.href = "/login.html";
        });
    }

    async function performLogin(payload, noticeId) {
        saveEmail(payload.email);
        const response = await callApi("/api/v1/auth/login", { method: "POST", body: payload });
        if (response && response.data) {
            saveTokens(response.data.accessToken, response.data.refreshToken);
            setNotice(noticeId, "로그인 성공. 제품 홈으로 이동합니다.");
            if (document.body.dataset.page === "home") {
                renderHomeState();
            } else {
                location.href = "/index.html";
            }
        }
    }

    function bindAuthForms(prefix, noticeId) {
        const sendVerificationId = prefix ? prefix + "SendVerificationForm" : "sendVerificationForm";
        const verifyId = prefix ? prefix + "VerifyForm" : "verifyForm";
        const registerId = prefix ? prefix + "RegisterForm" : "registerForm";
        const loginId = prefix ? prefix + "LoginForm" : "loginForm";

        bindForm(sendVerificationId, async function (form) {
            const payload = formJson(form);
            saveEmail(payload.email);
            await callApi("/api/v1/auth/verification/send", { method: "POST", body: payload });
            setNotice(noticeId, "인증 코드를 발송했습니다.");
        });

        bindForm(verifyId, async function (form) {
            const payload = formJson(form);
            saveEmail(payload.email);
            await callApi("/api/v1/auth/verification/verify", { method: "POST", body: payload });
            setNotice(noticeId, "이메일 인증이 완료되었습니다.");
        });

        bindForm(registerId, async function (form) {
            const payload = formJson(form);
            saveEmail(payload.email);
            await callApi("/api/v1/auth/register", { method: "POST", body: payload });
            setNotice(noticeId, "회원가입이 완료되었습니다.");
        });

        bindForm(loginId, async function (form) {
            await performLogin(formJson(form), noticeId);
        });
    }

    function initHomePage() {
        if (!requireAuth()) return;
        renderHomeState();

        bindButton("quickCreateExamBtn", async function () {
            if (!requireAuth()) return;
            const response = await callApi("/api/v1/exams", { method: "POST", auth: true, body: { scope: "PUBLIC" } });
            setText("dashboardExamId", String(response.data.examId));
            location.href = "/exams.html";
        });

        bindButton("quickPrivateExamBtn", async function () {
            if (!requireAuth()) return;
            const response = await callApi("/api/v1/exams", { method: "POST", auth: true, body: { scope: "PRIVATE" } });
            setText("dashboardExamId", String(response.data.examId));
            location.href = "/exams.html";
        });

        bindButton("quickHistoryBtn", async function () {
            if (!requireAuth()) return;
            const response = await callApi("/api/v1/exams/history?page=0&size=5", { method: "GET", auth: true });
            const count = response.data && response.data.content ? response.data.content.length : 0;
            setText("dashboardHistoryCount", String(count));
        });

    }

    function initLoginPage() {
        setupTabs();
        renderAuthState();
        bindAuthForms("", "loginNotice");

        bindForm("reissueForm", async function () {
            const response = await callApi("/api/v1/auth/reissue", {
                method: "POST",
                body: { refreshToken: refreshTokenValue() }
            });
            if (response && response.data) {
                saveTokens(response.data.accessToken, refreshTokenValue());
                setNotice("loginNotice", "액세스 토큰을 재발급했습니다.");
            }
        });
    }

    function toggleAdminOptionInputs() {
        const select = byId("adminQuestionType");
        const container = byId("adminOptionInputs");
        if (!select || !container) return;
        const isMultiple = select.value === "MULTIPLE_CHOICE";
        container.hidden = !isMultiple;
        container.querySelectorAll("input").forEach(function (input) {
            input.required = isMultiple;
        });
    }

    function initDocumentsPage() {
        if (!requireAuth()) return;
        renderDocumentSummary(null);

        bindForm("registerDocumentForm", async function (form) {
            const response = await callApi("/api/v1/my/documents", { method: "POST", auth: true, body: formJson(form) });
            renderDocumentSummary(response.data);
            subscribeDocumentEvents("my-documents", response.data.documentId, false);
        });

        bindForm("uploadDocumentForm", async function (form) {
            const response = await callApi("/api/v1/my/documents/upload", { method: "POST", auth: true, body: new FormData(form) });
            renderDocumentSummary(response.data);
            subscribeDocumentEvents("my-documents", response.data.documentId, false);
        });

        bindForm("documentStatusForm", async function (form) {
            const documentId = new FormData(form).get("documentId");
            const response = await callApi("/api/v1/my/documents/" + documentId + "/status", { method: "GET", auth: true });
            renderDocumentSummary(response.data);
            if (response.data.status === "PROCESSING" || response.data.status === "PENDING") {
                subscribeDocumentEvents("my-documents", response.data.documentId, false);
            }
        });
    }

    function initQuestionsPage() {
        if (!requireAuth()) return;
        renderQuestionList(null);
        renderQuestionDetail(null);

        bindForm("questionListForm", async function (form) {
            const payload = formJson(form);
            const params = new URLSearchParams();
            if (payload.type) params.set("type", payload.type);
            if (payload.difficulty) params.set("difficulty", payload.difficulty);
            if (payload.topic) params.set("topic", payload.topic);
            if (payload.scope) params.set("scope", payload.scope);
            params.set("page", payload.page || "0");
            params.set("size", payload.size || "10");
            const response = await callApi("/api/v1/questions?" + params.toString(), { method: "GET", auth: true });
            renderQuestionList(response.data);
        });

        bindForm("questionDetailForm", async function (form) {
            const questionId = new FormData(form).get("questionId");
            await loadQuestionDetail(questionId);
        });
    }

    function initExamsPage() {
        if (!requireAuth()) return;

        renderExamOverview(null);
        renderExamQuestions(null);
        renderExamResult(null);
        renderHistoryList(null);
        renderHistoryDetail(null);

        bindForm("createExamForm", async function () {
            const topicSelect = byId("examTopic");
            const scopeSelect = byId("examScope");
            const topic = topicSelect ? topicSelect.value : "";
            const scope = scopeSelect ? scopeSelect.value : "PUBLIC";
            const response = await callApi("/api/v1/exams", {
                method: "POST",
                auth: true,
                body: {
                    scope: scope,
                    topic: topic
                }
            });
            setText("activeExamId", String(response.data.examId));
            await loadExam(response.data.examId);
        });

        bindForm("getExamForm", async function (form) {
            const examId = new FormData(form).get("examId");
            await loadExam(examId);
        });

        bindButton("saveAnswersBtn", async function () {
            if (!state.currentExam) throw new Error("먼저 시험을 시작하거나 불러와야 합니다.");
            syncAnswersToState();
            await callApi("/api/v1/exams/" + state.currentExam.examId + "/answers", {
                method: "PUT",
                auth: true,
                body: collectAnswers()
            });
            renderExamQuestions(state.currentExam);
        });

        bindButton("submitExamBtn", async function () {
            if (!state.currentExam) throw new Error("먼저 시험을 시작하거나 불러와야 합니다.");
            syncAnswersToState();
            await callApi("/api/v1/exams/" + state.currentExam.examId + "/answers", {
                method: "PUT",
                auth: true,
                body: collectAnswers()
            });
            const response = await callApi("/api/v1/exams/" + state.currentExam.examId + "/submit", {
                method: "POST",
                auth: true
            });
            renderExamResult(response.data);
        });

        bindButton("examResultBtn", async function () {
            if (!state.currentExam) throw new Error("먼저 시험을 시작하거나 불러와야 합니다.");
            const response = await callApi("/api/v1/exams/" + state.currentExam.examId + "/result", {
                method: "GET",
                auth: true
            });
            renderExamResult(response.data);
        });

        bindForm("examHistoryForm", async function (form) {
            const payload = formJson(form);
            const params = new URLSearchParams({
                page: payload.page || "0",
                size: payload.size || "10"
            });
            const response = await callApi("/api/v1/exams/history?" + params.toString(), { method: "GET", auth: true });
            renderHistoryList(response.data);
        });

        bindForm("examHistoryDetailForm", async function (form) {
            const examId = new FormData(form).get("examId");
            await loadHistoryDetail(examId);
        });
    }

    function initAdminPage() {
        if (!requireAuth()) return;

        renderAdminResult(null, null);
        toggleAdminOptionInputs();

        const typeSelect = byId("adminQuestionType");
        if (typeSelect) {
            typeSelect.addEventListener("change", toggleAdminOptionInputs);
        }

        bindForm("adminUploadDocumentForm", async function (form) {
            const response = await callApi("/admin/v1/documents/upload", { method: "POST", auth: true, body: new FormData(form) });
            renderAdminResult("공용 문서 업로드 결과", response.data);
            subscribeDocumentEvents("admin-documents", response.data.documentId, true);
        });

        bindForm("adminRegisterDocumentForm", async function (form) {
            const response = await callApi("/admin/v1/documents", { method: "POST", auth: true, body: formJson(form) });
            renderAdminResult("공용 문서 등록 결과", response.data);
            subscribeDocumentEvents("admin-documents", response.data.documentId, true);
        });

        bindForm("adminDocumentStatusForm", async function (form) {
            const documentId = new FormData(form).get("documentId");
            const response = await callApi("/admin/v1/documents/" + documentId + "/status", { method: "GET", auth: true });
            renderAdminResult("공용 문서 상태", response.data);
            if (response.data.status === "PROCESSING" || response.data.status === "PENDING") {
                subscribeDocumentEvents("admin-documents", response.data.documentId, true);
            }
        });

        bindForm("adminCreateQuestionForm", async function (form) {
            const payload = formJson(form);
            const body = {
                topic: payload.topic,
                type: payload.type,
                difficulty: payload.difficulty,
                content: payload.content,
                answer: payload.answer,
                explanation: payload.explanation,
                options: payload.type === "MULTIPLE_CHOICE"
                    ? [payload.option1, payload.option2, payload.option3, payload.option4].filter(Boolean)
                    : []
            };
            const response = await callApi("/admin/v1/questions", { method: "POST", auth: true, body: body });
            renderAdminResult("공용 문제 등록 결과", response.data);
        });
    }

    initCommon();

    const page = document.body.dataset.page;
    if (page === "home") initHomePage();
    if (page === "login") initLoginPage();
    if (page === "documents") initDocumentsPage();
    if (page === "questions") initQuestionsPage();
    if (page === "exams") initExamsPage();
    if (page === "admin") initAdminPage();
})();
