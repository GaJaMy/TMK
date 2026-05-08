const loginTriggers = document.querySelectorAll("[data-login-trigger]");
const userLoginForm = document.getElementById("userLoginForm");
const userRegisterForm = document.getElementById("userRegisterForm");
const resetPasswordForm = document.getElementById("resetPasswordForm");
const userLoginStatus = document.getElementById("userLoginStatus");
const toggleRegisterButton = document.getElementById("toggleRegisterButton");
const resetPasswordButton = document.getElementById("resetPasswordButton");
const backToLoginButton = document.getElementById("backToLoginButton");
const loginUtilityLinks = document.getElementById("loginUtilityLinks");
const loginInlineSwitch = document.getElementById("loginInlineSwitch");
const authPanelKicker = document.getElementById("authPanelKicker");
const authPanelTitle = document.getElementById("authPanelTitle");
const authPanelDescription = document.getElementById("authPanelDescription");
const verifyResetUsernameButton = document.getElementById("verifyResetUsernameButton");
const resetPasswordFields = document.getElementById("resetPasswordFields");
const registerConfirmModal = document.getElementById("registerConfirmModal");
const closeRegisterConfirmModal = document.getElementById("closeRegisterConfirmModal");
const cancelRegisterConfirmButton = document.getElementById("cancelRegisterConfirmButton");
const confirmRegisterButton = document.getElementById("confirmRegisterButton");
const userNameDisplays = document.querySelectorAll("[data-user-username]");
const userLogoutButtons = document.querySelectorAll("[data-user-logout]");
const documentUploadForm = document.getElementById("documentUploadForm");
const documentTitleInput = document.getElementById("documentTitleInput");
const documentFileInput = document.getElementById("documentFileInput");
const documentFileName = document.getElementById("documentFileName");
const documentUploadStatus = document.getElementById("documentUploadStatus");
const documentProgressSummary = document.getElementById("documentProgressSummary");
const documentProgressSteps = document.querySelectorAll(".document-progress-step");
const documentListStatus = document.getElementById("documentListStatus");
const documentListEmptyState = document.getElementById("documentListEmptyState");
const documentList = document.getElementById("documentList");
const examStartForms = document.querySelectorAll("[data-exam-start-form]");
const examEmptyState = document.getElementById("examEmptyState");
const examActiveState = document.getElementById("examActiveState");
const examActiveTitle = document.getElementById("examActiveTitle");
const examActiveMeta = document.getElementById("examActiveMeta");
const resumeExamButton = document.getElementById("resumeExamButton");
const examCreatedSection = document.getElementById("examCreatedSection");
const examCreatedList = document.getElementById("examCreatedList");
const examEntryGrid = document.getElementById("examEntryGrid");
const examCreateStatus = document.getElementById("examCreateStatus");
const examStartDialog = document.getElementById("examStartDialog");
const examStartDialogTitle = document.getElementById("examStartDialogTitle");
const examStartDialogMessage = document.getElementById("examStartDialogMessage");
const closeExamStartDialog = document.getElementById("closeExamStartDialog");
const confirmExamStartDialog = document.getElementById("confirmExamStartDialog");
const examRoomContent = document.querySelectorAll("[data-exam-room-content]");
const examRoomEmptyState = document.getElementById("examRoomEmptyState");
const examRoomTitle = document.getElementById("examRoomTitle");
const examRemainingTime = document.getElementById("examRemainingTime");
const examRoomSourceType = document.getElementById("examRoomSourceType");
const examRoomQuestionCount = document.getElementById("examRoomQuestionCount");
const examRoomDuration = document.getElementById("examRoomDuration");
const examAnsweredCount = document.getElementById("examAnsweredCount");
const examQuestionList = document.getElementById("examQuestionList");
const submitExamButton = document.getElementById("submitExamButton");
const historyListStatus = document.getElementById("historyListStatus");
const historyListEmptyState = document.getElementById("historyListEmptyState");
const historyList = document.getElementById("historyList");
const historyDetailTitle = document.getElementById("historyDetailTitle");
const historyDetailMeta = document.getElementById("historyDetailMeta");
const historyDetailList = document.getElementById("historyDetailList");
const landingStatsStatus = document.getElementById("landingStatsStatus");
const landingStatDisplays = {
    accessAttempts: document.querySelector("[data-landing-stat='accessAttempts']"),
    examRuns: document.querySelector("[data-landing-stat='examRuns']"),
    documentRegistrations: document.querySelector("[data-landing-stat='documentRegistrations']"),
    questionGenerations: document.querySelector("[data-landing-stat='questionGenerations']")
};

const USER_SESSION_KEY = "tmk_user_username";
const USER_ACCESS_TOKEN_KEY = "tmk_user_access_token";
const USER_REFRESH_TOKEN_KEY = "tmk_user_refresh_token";
const USER_API_ORIGIN_KEY = "tmk_user_api_origin";
const EXAM_SESSION_KEY = "tmk_user_exam_session";
const EXAM_ANSWERS_KEY = "tmk_user_exam_answers";
const HISTORY_DETAIL_KEY = "tmk_user_history_detail";
const PUBLIC_PAGES = new Set(["index.html", "login.html"]);

let pendingRegisterPayload = null;
let examCountdownTimer = null;
let activeDocumentEventSource = null;
let activeDocumentId = null;
let currentDocuments = [];
let currentAvailableExams = [];
let currentExamHistory = [];
const currentPage = window.location.pathname.split("/").pop() || "index.html";

const getStoredUsername = () => window.sessionStorage.getItem(USER_SESSION_KEY);
const getStoredAccessToken = () => window.sessionStorage.getItem(USER_ACCESS_TOKEN_KEY);
const getStoredRefreshToken = () => window.sessionStorage.getItem(USER_REFRESH_TOKEN_KEY);
const getStoredApiOrigin = () => window.localStorage.getItem(USER_API_ORIGIN_KEY);

const getApiOrigin = () => {
    const configuredOrigin = getStoredApiOrigin();
    if (configuredOrigin) {
        return configuredOrigin.replace(/\/$/, "");
    }

    const { protocol, hostname, port } = window.location;
    const isLocalStaticPreview =
        protocol === "file:"
            || hostname === "localhost"
            || hostname === "127.0.0.1"
            || hostname === "::1";

    if (isLocalStaticPreview && port !== "8080") {
        return "http://localhost:8080";
    }

    return window.location.origin.replace(/\/$/, "");
};

const apiUrl = (path) => `${getApiOrigin()}${path}`;

const setStoredUserSession = ({ username, accessToken, refreshToken }) => {
    window.sessionStorage.setItem(USER_SESSION_KEY, username);
    window.sessionStorage.setItem(USER_ACCESS_TOKEN_KEY, accessToken);
    window.sessionStorage.setItem(USER_REFRESH_TOKEN_KEY, refreshToken);
};

const clearUserSession = () => {
    window.sessionStorage.removeItem(USER_SESSION_KEY);
    window.sessionStorage.removeItem(USER_ACCESS_TOKEN_KEY);
    window.sessionStorage.removeItem(USER_REFRESH_TOKEN_KEY);
};

const ensureAuthenticated = () => {
    if (PUBLIC_PAGES.has(currentPage)) {
        return true;
    }

    if (getStoredAccessToken()) {
        return true;
    }

    window.location.href = "./login.html";
    return false;
};

const getErrorMessage = (error) => {
    if (error instanceof Error) {
        return error.message;
    }
    return "알 수 없는 오류가 발생했습니다.";
};

const formatCount = (value) => Number(value || 0).toLocaleString("ko-KR");

const reissueUserSession = async () => {
    const refreshToken = getStoredRefreshToken();
    if (!refreshToken) {
        throw new Error("로그인이 만료되었습니다. 다시 로그인해 주세요.");
    }

    const response = await fetch(apiUrl("/api/auth/v1/reissue"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken })
    });

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        clearUserSession();
        throw new Error(payload?.msg || "로그인이 만료되었습니다. 다시 로그인해 주세요.");
    }

    const data = payload?.data ?? null;
    if (!data?.accessToken || !data?.refreshToken) {
        clearUserSession();
        throw new Error("토큰 재발급에 실패했습니다.");
    }

    setStoredUserSession({
        username: getStoredUsername() || "",
        accessToken: data.accessToken,
        refreshToken: data.refreshToken
    });
};

const request = async (path, options = {}) => {
    const { auth = true, headers = {}, body, skipReissue = false, ...rest } = options;
    const requestHeaders = new Headers(headers);
    const isFormData = body instanceof FormData;

    if (body !== undefined && !isFormData) {
        requestHeaders.set("Content-Type", "application/json");
    }

    if (auth) {
        const accessToken = getStoredAccessToken();
        if (accessToken) {
            requestHeaders.set("Authorization", `Bearer ${accessToken}`);
        }
    }

    const response = await fetch(apiUrl(path), {
        ...rest,
        headers: requestHeaders,
        body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body)
    });

    if (response.status === 204) {
        return null;
    }

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        const errorCode = payload?.errorCode;
        const message = payload?.msg || "요청 처리에 실패했습니다.";

        if (response.status === 401 && auth && !skipReissue && errorCode === "AUTH_002") {
            await reissueUserSession();
            return request(path, { ...options, skipReissue: true });
        }

        if (response.status === 401) {
            clearUserSession();
        }
        throw new Error(message);
    }

    return payload?.data ?? null;
};

const closeDocumentStatusStream = () => {
    if (!activeDocumentEventSource) {
        return;
    }

    activeDocumentEventSource.close();
    activeDocumentEventSource = null;
};

const resetStatus = () => {
    if (!userLoginStatus) {
        return;
    }
    userLoginStatus.classList.remove("is-success", "is-warning");
    userLoginStatus.textContent = "아이디와 비밀번호를 입력해 주세요.";
};

const setAuthMode = (mode) => {
    if (!userLoginForm || !userRegisterForm || !resetPasswordForm || !loginUtilityLinks || !loginInlineSwitch || !authPanelKicker || !authPanelTitle || !authPanelDescription) {
        return;
    }

    const isRegisterMode = mode === "register";
    const isResetMode = mode === "reset";
    userLoginForm.hidden = isRegisterMode || isResetMode;
    userRegisterForm.hidden = !isRegisterMode;
    resetPasswordForm.hidden = !isResetMode;
    loginUtilityLinks.hidden = isRegisterMode || isResetMode;
    loginInlineSwitch.hidden = !(isRegisterMode || isResetMode);

    if (isRegisterMode) {
        authPanelKicker.textContent = "REGISTER";
        authPanelTitle.textContent = "회원가입";
        authPanelDescription.textContent = "아이디, 비밀번호, 국가 코드를 입력해 계정을 생성합니다.";
    } else if (isResetMode) {
        authPanelKicker.textContent = "RESET";
        authPanelTitle.textContent = "비밀번호 재설정";
        authPanelDescription.textContent = "아이디를 확인한 뒤 새 비밀번호를 입력해 재설정합니다.";
    } else {
        authPanelKicker.textContent = "LOGIN";
        authPanelTitle.textContent = "로그인";
        authPanelDescription.textContent = "문서를 등록하고 나만의 문제를 생성하려면 로그인해야 합니다.";
    }

    if (resetPasswordFields) {
        resetPasswordFields.hidden = true;
    }

    if (resetPasswordForm && isResetMode) {
        resetPasswordForm.reset();
    }

    resetStatus();
};

const toggleRegisterConfirmModal = (isOpen) => {
    if (!registerConfirmModal) {
        return;
    }
    registerConfirmModal.hidden = !isOpen;
};

const applyUserIdentity = () => {
    const username = getStoredUsername() || "user";
    userNameDisplays.forEach((node) => {
        node.textContent = username;
    });
};

const renderLandingStats = (stats) => {
    if (!landingStatDisplays.accessAttempts) {
        return;
    }

    landingStatDisplays.accessAttempts.textContent = formatCount(stats.userPageAccessAttemptCount);
    landingStatDisplays.examRuns.textContent = formatCount(stats.examRunCount);
    landingStatDisplays.documentRegistrations.textContent = formatCount(stats.documentRegistrationCount);
    landingStatDisplays.questionGenerations.textContent = formatCount(stats.generatedPrivateQuestionCount);

    if (landingStatsStatus) {
        landingStatsStatus.textContent = "TMK 누적 이용 현황입니다.";
        landingStatsStatus.classList.remove("is-warning");
    }
};

const loadLandingStats = async () => {
    if (!landingStatDisplays.accessAttempts) {
        return;
    }

    if (landingStatsStatus) {
        landingStatsStatus.textContent = "서비스 이용 현황을 불러오는 중입니다.";
        landingStatsStatus.classList.remove("is-warning");
    }

    try {
        const stats = await request("/api/landing/stats", { auth: false });
        renderLandingStats(stats);
    } catch (error) {
        if (landingStatsStatus) {
            landingStatsStatus.textContent = `서비스 이용 현황을 불러오지 못했습니다: ${getErrorMessage(error)}`;
            landingStatsStatus.classList.add("is-warning");
        }
    }
};

const formatRemainingTime = (remainingMs) => {
    const totalSeconds = Math.max(0, Math.floor(remainingMs / 1000));
    const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
    const seconds = String(totalSeconds % 60).padStart(2, "0");
    return `${minutes}:${seconds}`;
};

const formatRemainingSeconds = (remainingSeconds) => formatRemainingTime(remainingSeconds * 1000);

const getExamSourceLabel = (sourceType) => {
    if (sourceType === "PUBLIC_TOPIC") {
        return "공용 문제 시험";
    }
    if (sourceType === "PRIVATE_DOCUMENT") {
        return "개인 문제 시험";
    }
    return sourceType || "";
};

const getExamSession = () => {
    const raw = window.sessionStorage.getItem(EXAM_SESSION_KEY);
    if (!raw) {
        return null;
    }

    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
};

const clearExamSession = () => {
    window.sessionStorage.removeItem(EXAM_SESSION_KEY);
    window.sessionStorage.removeItem(EXAM_ANSWERS_KEY);
};

const setExamSession = (examSession) => {
    window.sessionStorage.setItem(EXAM_SESSION_KEY, JSON.stringify(examSession));
};

const toggleExamStartDialog = (isOpen, title = "시험을 시작할 수 없습니다.", message = "시험 시작 조건을 확인한 뒤 다시 시도해 주세요.") => {
    if (!examStartDialog || !examStartDialogTitle || !examStartDialogMessage) {
        return;
    }
    examStartDialog.hidden = !isOpen;
    examStartDialogTitle.textContent = title;
    examStartDialogMessage.textContent = message;
};

const getExamAnswers = () => {
    const raw = window.sessionStorage.getItem(EXAM_ANSWERS_KEY);
    if (!raw) {
        return {};
    }

    try {
        return JSON.parse(raw);
    } catch {
        return {};
    }
};

const setExamAnswers = (answers) => {
    window.sessionStorage.setItem(EXAM_ANSWERS_KEY, JSON.stringify(answers));
};

const getActiveExamSession = () => {
    const examSession = getExamSession();
    if (!examSession) {
        return null;
    }

    if (Date.now() >= examSession.expiresAt) {
        clearExamSession();
        return null;
    }

    return examSession;
};

const updateExamAnsweredCount = () => {
    if (!examAnsweredCount || !examQuestionList) {
        return;
    }

    const examQuestionCards = examQuestionList.querySelectorAll(".exam-question-card");
    if (examQuestionCards.length === 0) {
        examAnsweredCount.textContent = "0 / 0";
        return;
    }

    const answers = getExamAnswers();
    const answered = Object.keys(answers).filter((key) => answers[key]).length;
    examAnsweredCount.textContent = `${answered} / ${examQuestionCards.length}`;
};

const applySavedExamAnswers = () => {
    if (!examQuestionList) {
        return;
    }

    const examQuestionCards = examQuestionList.querySelectorAll(".exam-question-card");
    if (examQuestionCards.length === 0) {
        return;
    }

    const answers = getExamAnswers();

    examQuestionCards.forEach((card) => {
        const questionId = card.dataset.questionId;
        const savedAnswer = answers[questionId];
        const badge = card.querySelector("[data-answer-badge]");
        const radioInputs = card.querySelectorAll("input[type='radio']");
        const textInput = card.querySelector("input[type='text']");

        if (savedAnswer) {
            if (badge) {
                badge.textContent = "입력 완료";
                badge.classList.add("is-confirmed");
            }
        }

        radioInputs.forEach((input) => {
            input.checked = input.value === savedAnswer;
        });

        if (textInput && savedAnswer) {
            textInput.value = savedAnswer;
        }
    });

    updateExamAnsweredCount();
};

const setExamAnswersFromServer = (questions) => {
    const storedAnswers = getExamAnswers();
    const serverAnswers = questions.reduce((accumulator, question) => {
        if (question.myAnswer) {
            accumulator[String(question.examQuestionId)] = question.myAnswer;
        }
        return accumulator;
    }, {});
    setExamAnswers({
        ...serverAnswers,
        ...storedAnswers
    });
};

const renderExamQuestions = (questions) => {
    if (!examQuestionList) {
        return;
    }

    examQuestionList.innerHTML = questions.map((question) => {
        const optionMarkup = question.type === "SHORT_ANSWER"
            ? `
                <label class="user-field">
                    <span>답안 입력</span>
                    <input type="text" name="answer-${question.examQuestionId}" placeholder="짧은 정답을 입력하세요">
                </label>
            `
            : question.options.map((option) => `
                <label class="exam-choice-option">
                    <input type="radio" name="answer-${question.examQuestionId}" value="${option.optionNumber}">
                    <span>${option.optionNumber}. ${option.content}</span>
                </label>
            `).join("");

        return `
            <article class="exam-question-card" data-question-id="${question.examQuestionId}">
                <div class="exam-question-head">
                    <div>
                        <span class="feature-index">QUESTION ${question.orderNum}</span>
                        <h2>${question.content}</h2>
                    </div>
                    <span class="exam-answer-badge" data-answer-badge>미입력</span>
                </div>
                <div class="exam-question-body">
                    ${optionMarkup}
                </div>
                <div class="exam-question-actions">
                    <button type="button" class="primary-cta primary-cta-confirm" data-answer-confirm>확인</button>
                </div>
            </article>
        `;
    }).join("");
};

const renderExamOverview = () => {
    if (!examEmptyState || !examEntryGrid) {
        return;
    }

    const inProgressExam = currentAvailableExams.find((exam) => exam.status === "IN_PROGRESS") || null;
    const createdExams = currentAvailableExams.filter((exam) => exam.status === "CREATED");

    if (!inProgressExam && createdExams.length === 0) {
        examEmptyState.hidden = false;
        if (examActiveState) {
            examActiveState.hidden = true;
        }
        if (examCreatedSection) {
            examCreatedSection.hidden = true;
        }
        examEntryGrid.hidden = false;
        return;
    }

    examEmptyState.hidden = true;
    examEntryGrid.hidden = false;

    if (examActiveState && examActiveTitle && examActiveMeta) {
        if (inProgressExam) {
            examActiveState.hidden = false;
            examActiveTitle.textContent = inProgressExam.title;
            examActiveMeta.textContent =
                `${inProgressExam.totalQuestions}문제 · 남은 시간 ${formatRemainingSeconds(inProgressExam.remainingSeconds)}`;
        } else {
            examActiveState.hidden = true;
        }
    }

    if (examCreatedSection && examCreatedList) {
        examCreatedSection.hidden = createdExams.length === 0;
        examCreatedList.innerHTML = createdExams.map((exam) => `
            <div class="history-row-item">
                <span class="history-row-title">${exam.title}</span>
                <span class="history-pass-badge">${exam.status}</span>
                <span class="history-row-meta">${exam.totalQuestions}문제</span>
                <span class="history-row-meta">${exam.timeLimitMinutes}분</span>
                <span class="history-row-meta">생성 ${formatDocumentDateTime(exam.createdAt)}</span>
                <button type="button" class="primary-cta" data-created-exam-id="${exam.examId}">시험 시작하기</button>
            </div>
        `).join("");
    }
};

const loadAvailableExams = async () => {
    const exams = await request("/exams");
    currentAvailableExams = Array.isArray(exams) ? exams : [];

    const activeExamSession = getExamSession();
    if (activeExamSession) {
        const matchedInProgressExam = currentAvailableExams.find((exam) =>
            exam.status === "IN_PROGRESS" && exam.examId === activeExamSession.examId
        );
        if (!matchedInProgressExam) {
            clearExamSession();
        }
    }

    renderExamOverview();
};

const startExamCountdown = (examSession) => {
    if (!examRemainingTime) {
        return;
    }

    if (examCountdownTimer) {
        window.clearInterval(examCountdownTimer);
    }

    const tick = () => {
        const remainingMs = examSession.expiresAt - Date.now();

        if (remainingMs <= 0) {
            examRemainingTime.textContent = "00:00";
            clearExamSession();
            if (examCountdownTimer) {
                window.clearInterval(examCountdownTimer);
            }
            return;
        }

        examRemainingTime.textContent = formatRemainingTime(remainingMs);
    };

    tick();
    examCountdownTimer = window.setInterval(tick, 1000);
};

const renderExamRoom = () => {
    if (examRoomContent.length === 0 || !examRoomEmptyState) {
        return;
    }

    const examSession = getActiveExamSession();

    if (!examSession) {
        examRoomContent.forEach((node) => {
            node.hidden = true;
        });
        examRoomEmptyState.hidden = false;
        return;
    }

    examRoomContent.forEach((node) => {
        node.hidden = false;
    });
    examRoomEmptyState.hidden = true;

    if (examRoomTitle) {
        examRoomTitle.textContent = examSession.title;
    }
    if (examRoomSourceType) {
        examRoomSourceType.textContent = examSession.sourceLabel;
    }
    if (examRoomQuestionCount) {
        examRoomQuestionCount.textContent = `${examSession.questionCount}문제`;
    }
    if (examRoomDuration) {
        examRoomDuration.textContent = `${examSession.durationMinutes}분`;
    }

    startExamCountdown(examSession);
    applySavedExamAnswers();
};

const buildExamSessionFromSummary = (exam) => ({
    examId: exam.examId,
    title: exam.title,
    sourceLabel: getExamSourceLabel(exam.sourceType),
    questionCount: exam.totalQuestions,
    durationMinutes: exam.timeLimitMinutes,
    expiresAt: exam.expiredAt ? new Date(exam.expiredAt).getTime() : 0
});

const buildExamSessionFromDetail = (exam) => ({
    examId: exam.examId,
    title: exam.title,
    sourceLabel: getExamSourceLabel(exam.sourceType),
    questionCount: exam.totalQuestions,
    durationMinutes: exam.timeLimitMinutes,
    expiresAt: exam.expiredAt ? new Date(exam.expiredAt).getTime() : 0
});

const loadExamRoom = async () => {
    if (currentPage !== "exam-room.html") {
        return;
    }

    let examSession = getActiveExamSession();

    if (!examSession) {
        const exams = await request("/exams");
        const inProgressExam = exams.find((exam) => exam.status === "IN_PROGRESS") || null;
        if (!inProgressExam) {
            renderExamRoom();
            return;
        }
        examSession = buildExamSessionFromSummary(inProgressExam);
        setExamSession(examSession);
    }

    try {
        const examDetail = await request(`/exams/${examSession.examId}`);
        setExamSession(buildExamSessionFromDetail(examDetail));
        setExamAnswersFromServer(examDetail.questions);
        renderExamQuestions(examDetail.questions);
        renderExamRoom();
    } catch (error) {
        const exams = await request("/exams");
        const inProgressExam = exams.find((exam) => exam.status === "IN_PROGRESS") || null;
        if (!inProgressExam || inProgressExam.examId !== examSession.examId) {
            clearExamSession();
        }
        renderExamRoom();
        if (examRoomEmptyState) {
            const title = examRoomEmptyState.querySelector("strong");
            const description = examRoomEmptyState.querySelector("p");
            if (title) {
                title.textContent = "진행중인 시험을 불러올 수 없습니다.";
            }
            if (description) {
                description.textContent = getErrorMessage(error);
            }
        }
    }
};

const handleExamRoomFailure = (message, redirectToExams = false) => {
    if (message) {
        window.alert(message);
    }
    if (redirectToExams) {
        clearExamSession();
        window.location.href = "./exams.html";
    }
};

const buildHistoryDetailPayloadFromResult = (result) => {
    const score = result.summary?.score ?? 0;
    const submittedAtText = result.submittedAt ? formatDocumentDateTime(result.submittedAt) : "-";
    const meta = `제출 완료 · ${submittedAtText} · ${result.totalQuestions}문제 · ${result.sourceType === "PUBLIC_TOPIC" ? "공용문제" : "개인문제"} · 정답률 ${score}%`;
    const questions = (result.questions || []).map((question) => ({
        label: `QUESTION ${question.orderNum}`,
        title: question.content,
        result: question.correct ? "정답" : "오답",
        resultClass: question.correct ? "is-correct" : "is-wrong",
        myAnswer: question.myAnswer || "미입력",
        correctAnswer: question.correctAnswer || "-",
        explanation: question.explanation || "-"
    }));

    return {
        title: result.title,
        meta,
        questions
    };
};

const renderExamHistory = (historyItems) => {
    if (!historyList || !historyListStatus || !historyListEmptyState) {
        return;
    }

    currentExamHistory = Array.isArray(historyItems) ? [...historyItems] : [];
    historyList.innerHTML = "";

    if (currentExamHistory.length === 0) {
        historyListStatus.textContent = "제출 완료된 시험이 없습니다.";
        historyListEmptyState.hidden = false;
        return;
    }

    historyListStatus.textContent = `${currentExamHistory.length}개의 시험 이력을 확인했습니다.`;
    historyListEmptyState.hidden = true;
    historyList.innerHTML = currentExamHistory.map((historyItem) => {
        const passLabel = historyItem.pass ? "합격" : "불합격";
        const passClassName = historyItem.pass ? "is-pass" : "is-fail";
        const sourceLabel = historyItem.sourceType === "PUBLIC_TOPIC" ? "공용문제" : "개인문제";
        return `
            <button type="button" class="history-row-item" data-history-exam-id="${historyItem.examId}">
                <span class="history-row-title">${historyItem.title}</span>
                <span class="history-pass-badge ${passClassName}">${passLabel}</span>
                <span class="history-row-meta">${formatDocumentDateTime(historyItem.submittedAt)}</span>
                <span class="history-row-meta">${historyItem.timeLimitMinutes}분</span>
                <span class="history-row-meta">${sourceLabel} · 정답 ${historyItem.correctCount}개 · ${historyItem.score}점</span>
            </button>
        `;
    }).join("");
};

const loadExamHistory = async () => {
    const historyItems = await request("/exams/history");
    renderExamHistory(historyItems);
};

const renderHistoryDetail = (item) => {
    if (!historyDetailTitle || !historyDetailMeta || !historyDetailList || !item) {
        return;
    }

    historyDetailTitle.textContent = item.dataset.historyTitle || "";
    historyDetailMeta.textContent = item.dataset.historyMeta || "";

    let questions = [];
    try {
        questions = JSON.parse(item.dataset.historyQuestions || "[]");
    } catch {
        questions = [];
    }

    historyDetailList.innerHTML = questions.map((question) => `
        <article class="history-question-card">
            <div class="history-question-head">
                <div>
                    <span class="feature-index">${question.label}</span>
                    <h3>${question.title}</h3>
                </div>
                <span class="history-result-badge ${question.resultClass}">${question.result}</span>
            </div>
            <dl class="history-answer-grid">
                <div>
                    <dt>내 답안</dt>
                    <dd>${question.myAnswer}</dd>
                </div>
                <div>
                    <dt>정답</dt>
                    <dd>${question.correctAnswer}</dd>
                </div>
            </dl>
            <div class="history-explanation-box">
                <strong>해설</strong>
                <p>${question.explanation}</p>
            </div>
        </article>
    `).join("");
};

const renderStoredHistoryDetail = () => {
    if (!historyDetailTitle || !historyDetailMeta || !historyDetailList) {
        return;
    }

    const raw = window.sessionStorage.getItem(HISTORY_DETAIL_KEY);
    if (!raw) {
        return;
    }

    try {
        const history = JSON.parse(raw);
        historyDetailTitle.textContent = history.title || "시험 상세 결과";
        historyDetailMeta.textContent = history.meta || "";
        const questions = Array.isArray(history.questions) ? history.questions : [];

        historyDetailList.innerHTML = questions.map((question) => `
            <article class="history-question-card">
                <div class="history-question-head">
                    <div>
                        <span class="feature-index">${question.label}</span>
                        <h3>${question.title}</h3>
                    </div>
                    <span class="history-result-badge ${question.resultClass}">${question.result}</span>
                </div>
                <dl class="history-answer-grid">
                    <div>
                        <dt>내 답안</dt>
                        <dd>${question.myAnswer}</dd>
                    </div>
                    <div>
                        <dt>정답</dt>
                        <dd>${question.correctAnswer}</dd>
                    </div>
                </dl>
                <div class="history-explanation-box">
                    <strong>해설</strong>
                    <p>${question.explanation}</p>
                </div>
            </article>
        `).join("");
    } catch {
        historyDetailList.innerHTML = "";
    }
};

const loadPublicTopics = async () => {
    const publicExamForm = document.querySelector("[data-exam-source='PUBLIC']");
    if (!publicExamForm) {
        return;
    }

    const topicSelect = publicExamForm.querySelector("select[name='sourceId']");
    if (!topicSelect) {
        return;
    }

    try {
        const topics = await request("/topics");
        const topicOptions = topics.map((topic) => `
            <option value="${topic.topicId}">${topic.name}</option>
        `);
        topicSelect.innerHTML = `
            <option value="">Topic을 선택하세요</option>
            ${topicOptions.join("")}
        `;
    } catch (error) {
        console.warn("공용 Topic 목록 조회 실패", error);
        topicSelect.innerHTML = `
            <option value="">Topic을 불러오지 못했습니다.</option>
        `;
    }
};

const loadPrivateDocumentOptions = async () => {
    const privateExamForm = document.querySelector("[data-exam-source='PRIVATE']");
    if (!privateExamForm) {
        return;
    }

    const documentSelect = privateExamForm.querySelector("select[name='sourceId']");
    if (!documentSelect) {
        return;
    }

    try {
        const documents = await request("/my/documents");
        const completedDocuments = documents.filter((documentItem) => documentItem.status === "COMPLETED");
        const documentOptions = completedDocuments.map((documentItem) => `
            <option value="${documentItem.documentId}">${documentItem.title} (${documentItem.generatedQuestionCount}문제)</option>
        `);
        documentSelect.innerHTML = `
            <option value="">문서 또는 문제 묶음을 선택하세요</option>
            ${documentOptions.join("")}
        `;
    } catch (error) {
        console.warn("완료 문서 목록 조회 실패", error);
        documentSelect.innerHTML = `
            <option value="">완료 문서를 불러오지 못했습니다.</option>
        `;
    }
};

if (loginTriggers.length > 0) {
    loginTriggers.forEach((button) => {
        button.addEventListener("click", () => {
            window.location.href = "./login.html";
        });
    });
}

if (userLoginForm && userLoginStatus) {
    userLoginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const userId = document.getElementById("userLoginId").value.trim();
        const password = document.getElementById("userLoginPassword").value.trim();

        userLoginStatus.classList.remove("is-success", "is-warning");

        if (!userId || !password) {
            userLoginStatus.textContent = "아이디와 비밀번호를 모두 입력해야 합니다.";
            userLoginStatus.classList.add("is-warning");
            return;
        }

        try {
            userLoginStatus.textContent = "로그인 중입니다.";
            const data = await request("/api/auth/v1/login", {
                method: "POST",
                auth: false,
                body: {
                    username: userId,
                    password
                }
            });
            setStoredUserSession({
                username: userId,
                accessToken: data.accessToken,
                refreshToken: data.refreshToken
            });
            userLoginStatus.textContent = "로그인에 성공했습니다. 사용자 홈으로 이동합니다.";
            userLoginStatus.classList.add("is-success");
            window.setTimeout(() => {
                window.location.href = "./home.html";
            }, 400);
        } catch (error) {
            userLoginStatus.textContent = `로그인 실패: ${getErrorMessage(error)}`;
            userLoginStatus.classList.add("is-warning");
        }
    });
}

if (userRegisterForm && userLoginStatus) {
    userRegisterForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const username = document.getElementById("registerUsername").value.trim();
        const password = document.getElementById("registerPassword").value.trim();
        const countryCode = document.getElementById("registerCountryCode").value.trim();

        userLoginStatus.classList.remove("is-success", "is-warning");

        if (!username || !password || !countryCode) {
            userLoginStatus.textContent = "회원가입에 필요한 정보를 모두 입력해야 합니다.";
            userLoginStatus.classList.add("is-warning");
            return;
        }

        pendingRegisterPayload = { username, password, countryCode };
        toggleRegisterConfirmModal(true);
    });
}

if (toggleRegisterButton) {
    toggleRegisterButton.addEventListener("click", () => {
        setAuthMode("register");
    });
}

if (resetPasswordButton) {
    resetPasswordButton.addEventListener("click", () => {
        setAuthMode("reset");
    });
}

if (backToLoginButton) {
    backToLoginButton.addEventListener("click", () => {
        setAuthMode("login");
    });
}

if (closeRegisterConfirmModal) {
    closeRegisterConfirmModal.addEventListener("click", () => {
        toggleRegisterConfirmModal(false);
    });
}

if (cancelRegisterConfirmButton) {
    cancelRegisterConfirmButton.addEventListener("click", () => {
        toggleRegisterConfirmModal(false);
    });
}

if (confirmRegisterButton && userRegisterForm && userLoginStatus) {
    confirmRegisterButton.addEventListener("click", async () => {
        if (!pendingRegisterPayload) {
            toggleRegisterConfirmModal(false);
            return;
        }

        userLoginStatus.classList.remove("is-warning", "is-success");
        userLoginStatus.textContent = "회원가입 요청을 전송하고 있습니다.";

        try {
            const created = await request("/api/auth/v1/register", {
                method: "POST",
                auth: false,
                body: pendingRegisterPayload
            });
            userLoginStatus.classList.add("is-success");
            userLoginStatus.textContent = `${created.username} 계정이 생성되었습니다. 로그인해 주세요.`;
            userRegisterForm.reset();
            pendingRegisterPayload = null;
            toggleRegisterConfirmModal(false);
            setAuthMode("login");
        } catch (error) {
            userLoginStatus.classList.add("is-warning");
            userLoginStatus.textContent = `회원가입 실패: ${getErrorMessage(error)}`;
            toggleRegisterConfirmModal(false);
        }
    });
}

if (userLogoutButtons.length > 0) {
    userLogoutButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                if (getStoredAccessToken()) {
                    await request("/api/auth/v1/logout", {
                        method: "POST"
                    });
                }
            } catch (error) {
                console.warn("로그아웃 요청 실패", error);
            } finally {
                closeDocumentStatusStream();
                clearUserSession();
                window.location.href = "./index.html";
            }
        });
    });
}

if (verifyResetUsernameButton && userLoginStatus) {
    verifyResetUsernameButton.addEventListener("click", () => {
        const username = document.getElementById("resetUsername").value.trim();

        userLoginStatus.classList.remove("is-success", "is-warning");

        if (!username) {
            userLoginStatus.textContent = "아이디를 입력해야 합니다.";
            userLoginStatus.classList.add("is-warning");
            return;
        }

        if (resetPasswordFields) {
            resetPasswordFields.hidden = false;
        }

        userLoginStatus.textContent = "새 비밀번호를 입력해 재설정을 진행해 주세요.";
        userLoginStatus.classList.add("is-success");
    });
}

if (resetPasswordForm && userLoginStatus) {
    resetPasswordForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = document.getElementById("resetUsername").value.trim();
        const newPassword = document.getElementById("resetNewPassword").value.trim();

        userLoginStatus.classList.remove("is-success", "is-warning");

        if (!username) {
            userLoginStatus.textContent = "아이디를 먼저 확인해야 합니다.";
            userLoginStatus.classList.add("is-warning");
            return;
        }

        if (!newPassword) {
            userLoginStatus.textContent = "새 비밀번호를 입력해야 합니다.";
            userLoginStatus.classList.add("is-warning");
            return;
        }

        try {
            await request("/api/auth/v1/reset-password", {
                method: "POST",
                auth: false,
                body: {
                    username,
                    newPassword
                }
            });
            userLoginStatus.textContent = "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해 주세요.";
            userLoginStatus.classList.add("is-success");
            setAuthMode("login");
        } catch (error) {
            userLoginStatus.textContent = `비밀번호 재설정 실패: ${getErrorMessage(error)}`;
            userLoginStatus.classList.add("is-warning");
        }
    });
}

const setDocumentProgress = (stage) => {
    if (!documentProgressSummary || documentProgressSteps.length === 0) {
        return;
    }

    const order = ["upload", "generating", "completed"];
    const currentIndex = order.indexOf(stage);

    documentProgressSteps.forEach((step, index) => {
        step.classList.remove("is-active", "is-completed");

        if (stage === "failed") {
            if (index === 0) {
                step.classList.add("is-completed");
            } else if (index === 1) {
                step.classList.add("is-active");
            }
            return;
        }

        if (index < currentIndex) {
            step.classList.add("is-completed");
        }

        if (index === currentIndex) {
            step.classList.add("is-active");
        }
    });

    if (stage === "upload") {
        documentProgressSummary.textContent = "문서를 업로드하고 있습니다.";
    } else if (stage === "generating") {
        documentProgressSummary.textContent = "업로드가 완료되어 AI가 문제를 생성하고 있습니다.";
    } else if (stage === "completed") {
        documentProgressSummary.textContent = "문제 생성이 완료되었습니다. 다음 시험 흐름으로 넘어갈 수 있습니다.";
    } else if (stage === "failed") {
        documentProgressSummary.textContent = "문제 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.";
    } else {
        documentProgressSummary.textContent = "아직 업로드가 시작되지 않았습니다.";
    }
};

const formatDocumentStatusLabel = (status) => {
    if (status === "PROCESSING") {
        return "생성중";
    }
    if (status === "COMPLETED") {
        return "완료";
    }
    if (status === "FAILED") {
        return "실패";
    }
    return status;
};

const formatDocumentDateTime = (value) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
};

const syncDocumentProgress = (documentStatus) => {
    if (!documentStatus) {
        setDocumentProgress();
        return;
    }

    if (documentStatus.status === "PROCESSING") {
        setDocumentProgress("generating");
        return;
    }

    if (documentStatus.status === "COMPLETED") {
        setDocumentProgress("completed");
        return;
    }

    if (documentStatus.status === "FAILED") {
        setDocumentProgress("failed");
        return;
    }

    setDocumentProgress();
};

const renderDocumentList = (documents) => {
    if (!documentList || !documentListEmptyState || !documentListStatus) {
        return;
    }

    documentList.innerHTML = "";

    currentDocuments = Array.isArray(documents) ? [...documents] : [];

    if (!documents || documents.length === 0) {
        documentListEmptyState.hidden = false;
        documentListStatus.textContent = "등록된 문서가 없습니다.";
        return;
    }

    documentListEmptyState.hidden = true;
        documentListStatus.textContent = "등록한 문서를 확인할 수 있습니다.";

    documents.forEach((documentItem) => {
        const item = document.createElement("li");
        item.className = "document-history-item";
        item.dataset.documentId = String(documentItem.documentId);
        if (activeDocumentId === documentItem.documentId) {
            item.classList.add("is-selected");
        }

        const main = document.createElement("div");
        main.className = "document-history-main";

        const title = document.createElement("strong");
        title.textContent = documentItem.title;

        const meta = document.createElement("p");
        meta.textContent = `마지막 변경 ${formatDocumentDateTime(documentItem.updatedAt)}`;

        main.append(title, meta);

        const badge = document.createElement("span");
        badge.className = "document-history-badge";
        badge.textContent = formatDocumentStatusLabel(documentItem.status);
        badge.classList.toggle("is-processing", documentItem.status === "PROCESSING");
        badge.classList.toggle("is-completed", documentItem.status === "COMPLETED");
        badge.classList.toggle("is-failed", documentItem.status === "FAILED");

        item.append(main, badge);
        documentList.append(item);
    });
};

const applyDocumentStatus = (documentStatus) => {
    if (!documentStatus) {
        return;
    }

    activeDocumentId = documentStatus.documentId;
    syncDocumentProgress(documentStatus);

    if (documentUploadStatus) {
        documentUploadStatus.classList.remove("is-success", "is-warning");
        if (documentStatus.status === "FAILED") {
            documentUploadStatus.textContent = "문제 생성에 실패했습니다.";
            documentUploadStatus.classList.add("is-warning");
        } else {
            documentUploadStatus.textContent = documentStatus.status === "COMPLETED"
                ? "문제 생성이 완료되었습니다."
                : "문제 생성이 진행 중입니다.";
            documentUploadStatus.classList.add("is-success");
        }
    }
};

const updateRenderedDocument = (documentStatus) => {
    if (!documentList) {
        return;
    }

    const updatedDocuments = [...currentDocuments];
    const targetIndex = updatedDocuments.findIndex((item) => item.documentId === documentStatus.documentId);
    if (targetIndex >= 0) {
        updatedDocuments[targetIndex] = documentStatus;
    } else {
        updatedDocuments.unshift(documentStatus);
    }

    renderDocumentList(updatedDocuments);
};

const subscribeDocumentStatus = (documentId) => {
    closeDocumentStatusStream();

    const accessToken = getStoredAccessToken();
    if (!accessToken) {
        return;
    }

    activeDocumentId = documentId;
    const eventSource = new EventSource(
        apiUrl(`/my/documents/${documentId}/events?accessToken=${encodeURIComponent(accessToken)}`)
    );

    eventSource.addEventListener("document-status", (event) => {
        const documentStatus = JSON.parse(event.data);
        applyDocumentStatus(documentStatus);
        updateRenderedDocument(documentStatus);

        if (documentStatus.status !== "PROCESSING") {
            closeDocumentStatusStream();
        }
    });

    eventSource.onerror = () => {
        closeDocumentStatusStream();
    };

    activeDocumentEventSource = eventSource;
};

const loadDocuments = async (focusedDocumentId = null) => {
    const documents = await request("/my/documents");
    if (focusedDocumentId !== null) {
        activeDocumentId = focusedDocumentId;
    } else if (!activeDocumentId && documents.length > 0) {
        activeDocumentId = documents[0].documentId;
    }

    renderDocumentList(documents);

    const selectedDocument = documents.find((item) => item.documentId === activeDocumentId) || documents[0];
    if (!selectedDocument) {
        activeDocumentId = null;
        syncDocumentProgress(null);
        closeDocumentStatusStream();
        return;
    }

    applyDocumentStatus(selectedDocument);
    if (selectedDocument.status === "PROCESSING") {
        subscribeDocumentStatus(selectedDocument.documentId);
    } else {
        closeDocumentStatusStream();
    }
};

if (documentFileInput && documentFileName) {
    documentFileInput.addEventListener("change", () => {
        const selectedFile = documentFileInput.files && documentFileInput.files[0];
        documentFileName.textContent = selectedFile ? selectedFile.name : "선택된 파일이 없습니다.";
    });
}

if (documentUploadForm && documentUploadStatus) {
    documentUploadForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const title = documentTitleInput ? documentTitleInput.value.trim() : "";
        const selectedFile = documentFileInput && documentFileInput.files ? documentFileInput.files[0] : null;

        documentUploadStatus.classList.remove("is-success", "is-warning");

        if (!title) {
            documentUploadStatus.textContent = "문서 제목을 입력해야 합니다.";
            documentUploadStatus.classList.add("is-warning");
            return;
        }

        if (!selectedFile) {
            documentUploadStatus.textContent = "먼저 위 영역에서 업로드할 문서를 선택해야 합니다.";
            documentUploadStatus.classList.add("is-warning");
            return;
        }

        const formData = new FormData();
        formData.append("title", title);
        formData.append("file", selectedFile);

        try {
            setDocumentProgress("upload");
            documentUploadStatus.textContent = "문서를 업로드하고 있습니다.";
            documentUploadStatus.classList.add("is-success");

            const uploaded = await request("/my/documents/upload", {
                method: "POST",
                body: formData
            });

            setDocumentProgress("generating");
            documentUploadStatus.textContent = "문서가 등록되었습니다. 문제 생성을 시작합니다.";
            documentUploadForm.reset();
            if (documentFileName) {
                documentFileName.textContent = "선택된 파일이 없습니다.";
            }
            await loadDocuments(uploaded.documentId);
            subscribeDocumentStatus(uploaded.documentId);
        } catch (error) {
            documentUploadStatus.textContent = `문서 등록 실패: ${getErrorMessage(error)}`;
            documentUploadStatus.classList.add("is-warning");
        }
    });
}

if (documentList) {
    documentList.addEventListener("click", async (event) => {
        const item = event.target.closest(".document-history-item");
        if (!item) {
            return;
        }

        const documentId = Number(item.dataset.documentId);
        if (!documentId) {
            return;
        }

        try {
            const documentStatus = await request(`/my/documents/${documentId}/status`);
            applyDocumentStatus(documentStatus);
            updateRenderedDocument(documentStatus);

            if (documentStatus.status === "PROCESSING") {
                subscribeDocumentStatus(documentId);
            } else {
                closeDocumentStatusStream();
            }
        } catch (error) {
            if (documentUploadStatus) {
                documentUploadStatus.classList.remove("is-success");
                documentUploadStatus.classList.add("is-warning");
                documentUploadStatus.textContent = `문서 상태 조회 실패: ${getErrorMessage(error)}`;
            }
        }
    });
}

if (examStartForms.length > 0) {
    loadPublicTopics();
    loadPrivateDocumentOptions();

    examStartForms.forEach((form) => {
        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const activeExam = getActiveExamSession();
            if (activeExam) {
                if (examCreateStatus) {
                    examCreateStatus.classList.remove("is-success");
                    examCreateStatus.classList.add("is-warning");
                    examCreateStatus.textContent = "이미 진행중인 시험이 있습니다. 먼저 재진입하거나 제출을 완료해 주세요.";
                }
                renderExamOverview();
                return;
            }

            const sourceType = form.dataset.examSource;
            const sourceId = Number(form.elements.sourceId.value);
            const sourceLabel = form.elements.sourceId.options[form.elements.sourceId.selectedIndex]?.text || "";
            const questionCount = Number(form.elements.questionCount.value);
            const durationMinutes = Number(form.elements.durationMinutes.value);
            if (!sourceId || !questionCount || !durationMinutes) {
                if (examCreateStatus) {
                    examCreateStatus.classList.remove("is-success");
                    examCreateStatus.classList.add("is-warning");
                    examCreateStatus.textContent = "시험 소스, 문제 수, 시험 시간을 모두 입력해야 합니다.";
                }
                return;
            }

            if (examCreateStatus) {
                examCreateStatus.classList.remove("is-success", "is-warning");
                examCreateStatus.textContent = "시험을 생성하고 있습니다.";
            }

            try {
                const requestBody = sourceType === "PUBLIC"
                    ? {
                        sourceType: "PUBLIC_TOPIC",
                        topicId: sourceId,
                        questionCount,
                        timeLimitMinutes: durationMinutes
                    }
                    : {
                        sourceType: "PRIVATE_DOCUMENT",
                        documentId: sourceId,
                        questionCount,
                        timeLimitMinutes: durationMinutes
                    };

                const createdExam = await request("/exams", {
                    method: "POST",
                    body: requestBody
                });

                if (examCreateStatus) {
                    examCreateStatus.classList.add("is-success");
                    examCreateStatus.textContent = `${sourceLabel} 시험이 생성되었습니다. 목록에서 시작할 수 있습니다.`;
                }
                form.reset();
                await loadAvailableExams();
            } catch (error) {
                if (examCreateStatus) {
                    examCreateStatus.classList.add("is-warning");
                    examCreateStatus.textContent = `시험 생성 실패: ${getErrorMessage(error)}`;
                }
            }
        });
    });
}

if (resumeExamButton) {
    resumeExamButton.addEventListener("click", () => {
        window.location.href = "./exam-room.html";
    });
}

if (examActiveMeta) {
    window.setInterval(() => {
        const currentInProgressExam = currentAvailableExams.find((exam) => exam.status === "IN_PROGRESS");
        if (!currentInProgressExam) {
            renderExamOverview();
            return;
        }

        currentAvailableExams = currentAvailableExams.map((exam) => {
            if (exam.examId !== currentInProgressExam.examId) {
                return exam;
            }
            return {
                ...exam,
                remainingSeconds: Math.max(0, exam.remainingSeconds - 1)
            };
        });
        const updatedInProgressExam = currentAvailableExams.find((exam) => exam.examId === currentInProgressExam.examId);
        examActiveMeta.textContent =
            `${updatedInProgressExam.totalQuestions}문제 · 남은 시간 ${formatRemainingSeconds(updatedInProgressExam.remainingSeconds)}`;

        if (updatedInProgressExam.remainingSeconds <= 0) {
            currentAvailableExams = currentAvailableExams.filter((exam) => exam.examId !== updatedInProgressExam.examId);
            renderExamOverview();
        }
    }, 1000);
}

if (examCreatedList) {
    examCreatedList.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-created-exam-id]");
        if (!button) {
            return;
        }
        const examId = Number(button.dataset.createdExamId);

        if (examCreateStatus) {
            examCreateStatus.classList.remove("is-success", "is-warning");
            examCreateStatus.textContent = "시험을 시작하고 있습니다.";
        }

        try {
            const startedExam = await request(`/exams/${examId}/start`, {
                method: "POST"
            });

            setExamSession({
                examId: startedExam.examId,
                title: startedExam.title,
                sourceLabel: getExamSourceLabel(startedExam.sourceType),
                questionCount: startedExam.totalQuestions,
                durationMinutes: startedExam.timeLimitMinutes,
                expiresAt: new Date(startedExam.expiredAt).getTime()
            });

            await loadAvailableExams();
            window.location.href = "./exam-room.html";
        } catch (error) {
            toggleExamStartDialog(true, "시험을 시작할 수 없습니다.", getErrorMessage(error));
            if (examCreateStatus) {
                examCreateStatus.classList.remove("is-success");
                examCreateStatus.classList.add("is-warning");
                examCreateStatus.textContent = `시험 시작 실패: ${getErrorMessage(error)}`;
            }
        }
    });
}

if (closeExamStartDialog) {
    closeExamStartDialog.addEventListener("click", () => toggleExamStartDialog(false));
}

if (confirmExamStartDialog) {
    confirmExamStartDialog.addEventListener("click", () => toggleExamStartDialog(false));
}

if (examStartDialog) {
    examStartDialog.addEventListener("click", (event) => {
        if (event.target === examStartDialog) {
            toggleExamStartDialog(false);
        }
    });
}

if (examQuestionList) {
    examQuestionList.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-answer-confirm]");
        if (!button) {
            return;
        }

        const card = button.closest(".exam-question-card");
        if (!card) {
            return;
        }

        const questionId = card.dataset.questionId;
        const badge = card.querySelector("[data-answer-badge]");
        const radioInputs = card.querySelectorAll("input[type='radio']");
        const textInput = card.querySelector("input[type='text']");

        let answerValue = "";

        if (radioInputs.length > 0) {
            const selected = Array.from(radioInputs).find((input) => input.checked);
            answerValue = selected ? selected.value : "";
        } else if (textInput) {
            answerValue = textInput.value.trim();
        }

        if (!answerValue) {
            return;
        }

        const examSession = getActiveExamSession();
        if (!examSession) {
            handleExamRoomFailure("진행중인 시험이 없습니다.", true);
            return;
        }

        try {
            await request(`/exams/${examSession.examId}/answers`, {
                method: "PUT",
                body: [
                    {
                        questionId: Number(questionId),
                        answer: answerValue
                    }
                ]
            });

            const answers = getExamAnswers();
            answers[questionId] = answerValue;
            setExamAnswers(answers);

            if (badge) {
                badge.textContent = "입력 완료";
                badge.classList.add("is-confirmed");
            }

            updateExamAnsweredCount();
        } catch (error) {
            const message = getErrorMessage(error);
            const shouldRedirect = message.includes("시험 시간이 만료");
            handleExamRoomFailure(message, shouldRedirect);
        }
    });
}

if (submitExamButton) {
    submitExamButton.addEventListener("click", async () => {
        const examSession = getActiveExamSession();
        if (!examSession) {
            clearExamSession();
            window.location.href = "./exams.html";
            return;
        }

        try {
            await request(`/exams/${examSession.examId}/submit`, {
                method: "POST"
            });
            const result = await request(`/exams/${examSession.examId}/result`);
            const historyDetailPayload = buildHistoryDetailPayloadFromResult(result);
            window.sessionStorage.setItem(HISTORY_DETAIL_KEY, JSON.stringify(historyDetailPayload));
            clearExamSession();
            window.location.href = "./exam-result.html";
        } catch (error) {
            const message = getErrorMessage(error);
            const shouldRedirect = message.includes("시험 시간이 만료");
            if (shouldRedirect) {
                try {
                    const result = await request(`/exams/${examSession.examId}/result`);
                    const historyDetailPayload = buildHistoryDetailPayloadFromResult(result);
                    window.sessionStorage.setItem(HISTORY_DETAIL_KEY, JSON.stringify(historyDetailPayload));
                    clearExamSession();
                    window.location.href = "./exam-result.html";
                    return;
                } catch {
                    clearExamSession();
                    window.alert("시험 시간이 만료되어 자동 제출되었습니다.");
                    window.location.href = "./exams.html";
                    return;
                }
            }
            window.alert(message);
        }
    });
}

if (historyList) {
    historyList.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-history-exam-id]");
        if (!button) {
            return;
        }

        const examId = Number(button.dataset.historyExamId);
        if (!Number.isFinite(examId)) {
            return;
        }

        if (historyListStatus) {
            historyListStatus.textContent = "시험 결과를 불러오는 중입니다.";
        }

        try {
            const result = await request(`/exams/${examId}/result`);
            const historyDetailPayload = buildHistoryDetailPayloadFromResult(result);
            window.sessionStorage.setItem(HISTORY_DETAIL_KEY, JSON.stringify(historyDetailPayload));
            window.location.href = "./history-detail.html";
        } catch (error) {
            if (historyListStatus) {
                historyListStatus.textContent = `시험 결과 조회 실패: ${getErrorMessage(error)}`;
            }
        }
    });
}

if (!ensureAuthenticated()) {
    // redirected
} else if (userNameDisplays.length > 0) {
    applyUserIdentity();

    if (currentPage === "documents.html" && documentUploadForm) {
        loadDocuments().catch((error) => {
            if (documentUploadStatus) {
                documentUploadStatus.classList.remove("is-success");
                documentUploadStatus.classList.add("is-warning");
                documentUploadStatus.textContent = `문서 목록 조회 실패: ${getErrorMessage(error)}`;
            }
        });
    }

    if (currentPage === "exams.html") {
        loadAvailableExams().catch((error) => {
            if (examCreateStatus) {
                examCreateStatus.classList.remove("is-success");
                examCreateStatus.classList.add("is-warning");
                examCreateStatus.textContent = `시험 목록 조회 실패: ${getErrorMessage(error)}`;
            }
        });
    }

    if (currentPage === "history.html") {
        loadExamHistory().catch((error) => {
            if (historyListStatus) {
                historyListStatus.textContent = `시험 이력 조회 실패: ${getErrorMessage(error)}`;
            }
        });
    }
}

if (currentPage === "index.html") {
    loadLandingStats();
}

renderExamOverview();
loadExamRoom().catch((error) => {
    clearExamSession();
    renderExamRoom();
    if (examRoomEmptyState) {
        const title = examRoomEmptyState.querySelector("strong");
        const description = examRoomEmptyState.querySelector("p");
        if (title) {
            title.textContent = "진행중인 시험을 불러올 수 없습니다.";
        }
        if (description) {
            description.textContent = getErrorMessage(error);
        }
    }
});
renderStoredHistoryDetail();
