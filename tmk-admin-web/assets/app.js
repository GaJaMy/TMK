const ADMIN_SESSION_KEY = "tmk_admin_username";
const ADMIN_ACCESS_TOKEN_KEY = "tmk_admin_access_token";
const ADMIN_REFRESH_TOKEN_KEY = "tmk_admin_refresh_token";
const ADMIN_API_ORIGIN_KEY = "tmk_admin_api_origin";

const QUESTION_TYPE_LABELS = {
    MULTIPLE_CHOICE: "객관식",
    SHORT_ANSWER: "단답형",
    TRUE_FALSE: "참/거짓"
};

const QUESTION_TYPE_VALUES = {
    "객관식": "MULTIPLE_CHOICE",
    "단답형": "SHORT_ANSWER",
    "참/거짓": "TRUE_FALSE"
};

const DIFFICULTY_LABELS = {
    EASY: "하",
    NORMAL: "중",
    HARD: "상"
};

const DIFFICULTY_VALUES = {
    "하": "EASY",
    "중": "NORMAL",
    "상": "HARD"
};

const loginForm = document.getElementById("adminLoginForm");
const statusCard = document.getElementById("loginStatus");
const adminCreateForm = document.getElementById("adminCreateForm");
const adminCreateStatus = document.getElementById("adminCreateStatus");
const openAdminModalButton = document.getElementById("openAdminModal");
const appendAdminRow = document.getElementById("appendAdminRow");
const closeAdminModalButton = document.getElementById("closeAdminModal");
const cancelAdminModalButton = document.getElementById("cancelAdminModal");
const adminModalBackdrop = document.getElementById("adminModalBackdrop");
const adminTableBody = document.getElementById("adminTableBody");
const rangeButtons = document.querySelectorAll(".range-button");
const monitoringDescription = document.getElementById("monitoringDescription");
const topicFilter = document.getElementById("topicFilter");
const questionListDescription = document.getElementById("questionListDescription");
const questionPageStatus = document.getElementById("questionPageStatus");
const publicQuestionTableBody = document.getElementById("publicQuestionTableBody");
const tabButtons = document.querySelectorAll(".tab-button");
const tabPanels = document.querySelectorAll(".tab-panel");
const openTopicModalButton = document.getElementById("openTopicModal");
const openQuestionModalButton = document.getElementById("openQuestionModal");
const appendQuestionRow = document.getElementById("appendQuestionRow");
const appendTopicRow = document.getElementById("appendTopicRow");
const topicModalBackdrop = document.getElementById("topicModalBackdrop");
const closeTopicModalButton = document.getElementById("closeTopicModal");
const cancelTopicModalButton = document.getElementById("cancelTopicModal");
const topicCreateForm = document.getElementById("topicCreateForm");
const questionModalBackdrop = document.getElementById("questionModalBackdrop");
const closeQuestionModalButton = document.getElementById("closeQuestionModal");
const cancelQuestionModalButton = document.getElementById("cancelQuestionModal");
const questionCreateForm = document.getElementById("questionCreateForm");
const questionOptionsSection = document.getElementById("questionOptionsSection");
const questionOptionsContainer = document.getElementById("questionOptionsContainer");
const questionTypeSelect = document.getElementById("newQuestionType");
const questionAnswerInput = document.getElementById("newQuestionAnswer");
const questionAnswerSelect = document.getElementById("newQuestionAnswerSelect");
const questionDetailModal = document.getElementById("questionDetailModal");
const closeQuestionDetailModalButton = document.getElementById("closeQuestionDetailModal");
const cancelQuestionDetailModalButton = document.getElementById("cancelQuestionDetailModal");
const detailQuestion = document.getElementById("detailQuestion");
const detailType = document.getElementById("detailType");
const detailLevel = document.getElementById("detailLevel");
const detailTopic = document.getElementById("detailTopic");
const detailAnswer = document.getElementById("detailAnswer");
const detailExplanation = document.getElementById("detailExplanation");
const detailToggleStatusButton = document.getElementById("detailToggleStatusButton");
const detailDeleteButton = document.getElementById("detailDeleteButton");
const selectAllQuestions = document.getElementById("selectAllQuestions");
const bulkEnableButton = document.getElementById("bulkEnableButton");
const bulkDisableButton = document.getElementById("bulkDisableButton");
const bulkDeleteButton = document.getElementById("bulkDeleteButton");
const topicTableBody = document.getElementById("topicTableBody");
const adminUsernameDisplays = document.querySelectorAll("[data-admin-username]");
const adminLogoutButtons = document.querySelectorAll("[data-admin-logout]");
const accessAttemptMetric = document.getElementById("accessAttemptMetric");
const examRunMetric = document.getElementById("examRunMetric");
const documentRegistrationMetric = document.getElementById("documentRegistrationMetric");
const questionGenerationMetric = document.getElementById("questionGenerationMetric");
const accessAttemptsChart = document.getElementById("accessAttemptsChart");
const examRunsChart = document.getElementById("examRunsChart");
const documentRegistrationsChart = document.getElementById("documentRegistrationsChart");
const questionGenerationsChart = document.getElementById("questionGenerationsChart");

let selectedQuestionRow = null;
let selectedQuestionDetail = null;
let cachedTopics = [];
let cachedQuestions = [];
let currentMonitoringRange = "일별";

const getStoredAdminUsername = () => window.sessionStorage.getItem(ADMIN_SESSION_KEY);
const getStoredAccessToken = () => window.sessionStorage.getItem(ADMIN_ACCESS_TOKEN_KEY);
const getStoredRefreshToken = () => window.sessionStorage.getItem(ADMIN_REFRESH_TOKEN_KEY);
const getStoredApiOrigin = () => window.localStorage.getItem(ADMIN_API_ORIGIN_KEY);

const setStoredSession = ({ username, accessToken, refreshToken }) => {
    window.sessionStorage.setItem(ADMIN_SESSION_KEY, username);
    window.sessionStorage.setItem(ADMIN_ACCESS_TOKEN_KEY, accessToken);
    window.sessionStorage.setItem(ADMIN_REFRESH_TOKEN_KEY, refreshToken);
};

const clearAdminIdentity = () => {
    window.sessionStorage.removeItem(ADMIN_SESSION_KEY);
    window.sessionStorage.removeItem(ADMIN_ACCESS_TOKEN_KEY);
    window.sessionStorage.removeItem(ADMIN_REFRESH_TOKEN_KEY);
};

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

const isProtectedPage = () => !loginForm;

const ensureAuthenticated = () => {
    if (!isProtectedPage()) {
        return true;
    }

    if (getStoredAccessToken()) {
        return true;
    }

    window.location.href = "./index.html";
    return false;
};

const formatDate = (value) => {
    if (!value) {
        return "-";
    }

    return value.slice(0, 10);
};

const setStatus = (message, kind) => {
    if (!statusCard) {
        return;
    }

    statusCard.textContent = message;
    statusCard.classList.remove("is-success", "is-warning");

    if (kind) {
        statusCard.classList.add(kind);
    }
};

const setAdminCreateStatus = (message, kind) => {
    if (!adminCreateStatus) {
        return;
    }

    adminCreateStatus.textContent = message;
    adminCreateStatus.classList.remove("is-success", "is-warning");
    if (kind) {
        adminCreateStatus.classList.add(kind);
    }
};

const setQuestionPageStatus = (message, kind) => {
    if (!questionPageStatus) {
        return;
    }

    questionPageStatus.textContent = message;
    questionPageStatus.classList.remove("is-success", "is-warning");
    if (kind) {
        questionPageStatus.classList.add(kind);
    }
};

const applyAdminIdentity = () => {
    const username = getStoredAdminUsername() || "admin";
    adminUsernameDisplays.forEach((node) => {
        node.textContent = username;
    });
};

const toggleAdminModal = (isOpen) => {
    if (adminModalBackdrop) {
        adminModalBackdrop.hidden = !isOpen;
    }
};

const toggleTopicModal = (isOpen) => {
    if (topicModalBackdrop) {
        topicModalBackdrop.hidden = !isOpen;
    }
};

const toggleQuestionModal = (isOpen) => {
    if (questionModalBackdrop) {
        questionModalBackdrop.hidden = !isOpen;
    }
};

const toggleQuestionDetailModal = (isOpen) => {
    if (questionDetailModal) {
        questionDetailModal.hidden = !isOpen;
    }
};

const getErrorMessage = (error) => {
    if (error instanceof Error) {
        return error.message;
    }
    return "알 수 없는 오류가 발생했습니다.";
};

const reissueAdminSession = async () => {
    const refreshToken = getStoredRefreshToken();
    if (!refreshToken) {
        throw new Error("로그인이 만료되었습니다. 다시 로그인해 주세요.");
    }

    const response = await fetch(apiUrl("/admin/auth/v1/reissue"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken })
    });

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        clearAdminIdentity();
        throw new Error(payload?.msg || "로그인이 만료되었습니다. 다시 로그인해 주세요.");
    }

    const data = payload?.data ?? null;
    if (!data?.accessToken || !data?.refreshToken) {
        clearAdminIdentity();
        throw new Error("토큰 재발급에 실패했습니다.");
    }

    setStoredSession({
        username: data.username,
        accessToken: data.accessToken,
        refreshToken: data.refreshToken
    });
};

const request = async (path, options = {}) => {
    const { auth = true, headers = {}, body, skipReissue = false, ...rest } = options;
    const requestHeaders = new Headers(headers);

    if (body !== undefined) {
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
        body: body !== undefined ? JSON.stringify(body) : undefined
    });

    if (response.status === 204) {
        return null;
    }

    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        const errorCode = payload?.errorCode;
        const message = payload?.msg || "요청 처리에 실패했습니다.";

        if (response.status === 401 && auth && !skipReissue && errorCode === "AUTH_002") {
            await reissueAdminSession();
            return request(path, { ...options, skipReissue: true });
        }

        if (response.status === 401) {
            clearAdminIdentity();
        }
        throw new Error(message);
    }

    return payload?.data ?? null;
};

const getVisibleQuestionRows = () => {
    if (!publicQuestionTableBody) {
        return [];
    }

    return Array.from(publicQuestionTableBody.querySelectorAll("tr[data-question-id]")).filter((row) => !row.hidden);
};

const getCheckedQuestionRows = () =>
    getVisibleQuestionRows().filter((row) => row.querySelector(".row-checkbox")?.checked);

const updateMonitoringDescription = () => {
    if (!monitoringDescription) {
        return;
    }

    monitoringDescription.textContent =
        `사용자 접근과 학습 활동을 ${currentMonitoringRange} 기준으로 확인하는 관리자 메인페이지입니다.`;
};

const renderSeriesChart = (container, series) => {
    if (!container) {
        return;
    }

    if (!series || series.length === 0) {
        container.className = "chart-empty";
        container.textContent = "표시할 데이터가 없습니다.";
        return;
    }

    const hasValue = series.some((item) => item.count > 0);
    if (!hasValue) {
        container.className = "chart-empty";
        container.textContent = "선택한 기간에 집계된 데이터가 없습니다.";
        return;
    }

    const maxCount = Math.max(...series.map((item) => item.count));
    const columns = series.map((item) => {
        const height = `${Math.max((item.count / maxCount) * 100, item.count > 0 ? 8 : 0)}%`;
        return `
            <div class="chart-column">
                <strong class="chart-column-count">${item.count}</strong>
                <div class="chart-column-track">
                    <div class="chart-column-fill" style="height:${height}"></div>
                </div>
                <span class="chart-column-label">${item.label}</span>
            </div>
        `;
    });

    container.className = "chart-canvas";
    container.innerHTML = `<div class="chart-columns">${columns.join("")}</div>`;
};

const getPeriodRequest = (range) => {
    const today = new Date();
    const clone = (date) => new Date(date.getTime());
    const toLocalDateString = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    };

    if (range === "주별") {
        const current = clone(today);
        const day = current.getDay();
        const diffToMonday = day === 0 ? -6 : 1 - day;
        current.setDate(current.getDate() + diffToMonday);
        const from = clone(current);
        from.setDate(from.getDate() - 21);
        const to = clone(from);
        to.setDate(from.getDate() + 27);
        return { periodType: "WEEKLY", from: toLocalDateString(from), to: toLocalDateString(to) };
    }

    if (range === "월별") {
        const from = new Date(today.getFullYear(), today.getMonth() - 5, 1);
        const to = new Date(today.getFullYear(), today.getMonth() + 1, 0);
        return { periodType: "MONTHLY", from: toLocalDateString(from), to: toLocalDateString(to) };
    }

    const from = clone(today);
    from.setDate(from.getDate() - 6);
    return { periodType: "DAILY", from: toLocalDateString(from), to: toLocalDateString(today) };
};

const buildMonitoringQuery = (periodRequest) => {
    const params = new URLSearchParams(periodRequest);
    return `?${params.toString()}`;
};

const loadMonitoringStats = async () => {
    const periodRequest = getPeriodRequest(currentMonitoringRange);
    updateMonitoringDescription();

    try {
        const query = buildMonitoringQuery(periodRequest);
        const [accessAttempts, examRuns, documentRegistrations, questionGenerations] = await Promise.all([
            request(`/admin/v1/monitoring/access-attempts${query}`),
            request(`/admin/v1/monitoring/exam-runs${query}`),
            request(`/admin/v1/monitoring/document-registrations${query}`),
            request(`/admin/v1/monitoring/question-generations${query}`)
        ]);

        if (accessAttemptMetric) accessAttemptMetric.textContent = accessAttempts.summary.totalCount.toLocaleString();
        if (examRunMetric) examRunMetric.textContent = examRuns.summary.totalCount.toLocaleString();
        if (documentRegistrationMetric) documentRegistrationMetric.textContent = documentRegistrations.summary.totalCount.toLocaleString();
        if (questionGenerationMetric) questionGenerationMetric.textContent = questionGenerations.summary.totalCount.toLocaleString();

        renderSeriesChart(accessAttemptsChart, accessAttempts.series);
        renderSeriesChart(examRunsChart, examRuns.series);
        renderSeriesChart(documentRegistrationsChart, documentRegistrations.series);
        renderSeriesChart(questionGenerationsChart, questionGenerations.series);
    } catch (error) {
        const message = getErrorMessage(error);
        if (accessAttemptMetric) accessAttemptMetric.textContent = "오류";
        if (examRunMetric) examRunMetric.textContent = "오류";
        if (documentRegistrationMetric) documentRegistrationMetric.textContent = "오류";
        if (questionGenerationMetric) questionGenerationMetric.textContent = "오류";
        renderSeriesChart(accessAttemptsChart, []);
        renderSeriesChart(examRunsChart, []);
        renderSeriesChart(documentRegistrationsChart, []);
        renderSeriesChart(questionGenerationsChart, []);
        if (monitoringDescription) {
            monitoringDescription.textContent = `모니터링 통계를 불러오지 못했습니다. ${message}`;
        }
    }
};

const renderAdminRows = (admins) => {
    if (!adminTableBody || !appendAdminRow) {
        return;
    }

    const rows = admins.map((admin) => `
        <tr data-admin-id="${admin.adminId}">
            <td>${admin.username}</td>
            <td><span class="table-badge ${admin.active ? "is-active" : "is-inactive"}">${admin.active ? "활성" : "비활성"}</span></td>
            <td>${formatDate(admin.createdAt)}</td>
            <td class="table-actions">
                <button type="button" class="ghost-button" data-action="${admin.active ? "disable" : "enable"}">
                    ${admin.active ? "비활성화" : "활성화"}
                </button>
                <button type="button" class="ghost-button is-danger" data-action="delete">삭제</button>
            </td>
        </tr>
    `);

    adminTableBody.innerHTML = `${rows.join("")}${appendAdminRow.outerHTML}`;
};

const loadAdmins = async () => {
    try {
        const admins = await request("/admin/v1/users");
        renderAdminRows(admins);
        setAdminCreateStatus("관리자 목록을 확인했습니다.", "is-success");
    } catch (error) {
        setAdminCreateStatus(`관리자 목록 조회 실패: ${getErrorMessage(error)}`, "is-warning");
    }
};

const renderTopicOptions = () => {
    if (!topicFilter) {
        return;
    }

    const options = cachedTopics.map((topic) => `<option value="${topic.topicId}">${topic.name}</option>`);
    topicFilter.innerHTML = `<option value="all">전체</option>${options.join("")}`;

    const questionTopicSelect = document.getElementById("newQuestionTopic");
    if (questionTopicSelect) {
        questionTopicSelect.innerHTML = cachedTopics.length === 0
            ? `<option value="">Topic을 먼저 추가하세요</option>`
            : cachedTopics.map((topic) => `<option value="${topic.topicId}">${topic.name}</option>`).join("");
    }
};

const renderTopicRows = () => {
    if (!topicTableBody || !appendTopicRow) {
        return;
    }

    const rows = cachedTopics.map((topic) => `
        <tr data-topic-id="${topic.topicId}" data-topic-name="${topic.name}">
            <td>${topic.name}</td>
            <td>${topic.questionCount}</td>
            <td>${formatDate(topic.createdAt)}</td>
            <td class="table-actions">
                <button type="button" class="ghost-button is-danger" data-action="delete-topic">삭제</button>
            </td>
        </tr>
    `);

    topicTableBody.innerHTML = `${rows.join("")}${appendTopicRow.outerHTML}`;
};

const buildQuestionRow = (question) => `
    <tr
        data-question-id="${question.questionId}"
        data-topic-id="${question.topicId}"
        data-question="${question.content}"
        data-type="${QUESTION_TYPE_LABELS[question.type] ?? question.type}"
        data-level="${DIFFICULTY_LABELS[question.difficulty] ?? question.difficulty}"
        data-topic-name="${question.topicName}"
        data-answer=""
        data-explanation=""
        data-options=""
    >
        <td><input type="checkbox" class="row-checkbox" aria-label="문제 선택"></td>
        <td>${question.content}</td>
        <td>${QUESTION_TYPE_LABELS[question.type] ?? question.type}</td>
        <td>${DIFFICULTY_LABELS[question.difficulty] ?? question.difficulty}</td>
        <td>${question.topicName}</td>
        <td><span class="table-badge ${question.active ? "is-active" : "is-inactive"}">${question.active ? "활성" : "비활성"}</span></td>
        <td>${formatDate(question.createdAt)}</td>
        <td class="table-actions">
            <button type="button" class="ghost-button" data-action="${question.active ? "disable" : "enable"}">
                ${question.active ? "비활성화" : "활성화"}
            </button>
            <button type="button" class="ghost-button is-danger" data-action="delete">삭제</button>
        </td>
    </tr>
`;

const renderQuestionRows = () => {
    if (!publicQuestionTableBody || !appendQuestionRow) {
        return;
    }

    const rows = cachedQuestions.map((question) => buildQuestionRow(question));
    publicQuestionTableBody.innerHTML = `${rows.join("")}${appendQuestionRow.outerHTML}`;
    applyQuestionFilter();
};

const loadTopics = async () => {
    cachedTopics = await request("/admin/v1/topics");
    renderTopicOptions();
    renderTopicRows();
};

const loadQuestions = async () => {
    const topicId = topicFilter && topicFilter.value !== "all" ? topicFilter.value : null;
    const query = topicId ? `?topicId=${encodeURIComponent(topicId)}` : "";
    cachedQuestions = await request(`/admin/v1/questions${query}`);
    renderQuestionRows();
};

const loadQuestionPage = async () => {
    setQuestionPageStatus("공용 문제와 Topic을 불러오는 중입니다.");
    try {
        await loadTopics();
        await loadQuestions();
        setQuestionPageStatus("공용 문제와 Topic을 확인했습니다.", "is-success");
    } catch (error) {
        setQuestionPageStatus(`질문 관리 데이터 조회 실패: ${getErrorMessage(error)}`, "is-warning");
    }
};

const updateQuestionOptionInputs = () => {
    if (!questionTypeSelect || !questionOptionsSection || !questionOptionsContainer || !questionAnswerInput || !questionAnswerSelect) {
        return;
    }

    const typeValue = questionTypeSelect.value;
    const type = QUESTION_TYPE_VALUES[typeValue];
    questionOptionsContainer.innerHTML = "";

    if (type === "SHORT_ANSWER") {
        questionOptionsSection.hidden = true;
        questionAnswerInput.hidden = false;
        questionAnswerInput.required = true;
        questionAnswerSelect.hidden = true;
        questionAnswerSelect.required = false;
        questionAnswerSelect.innerHTML = "";
        return;
    }

    questionOptionsSection.hidden = false;
    questionAnswerInput.hidden = true;
    questionAnswerInput.required = false;
    questionAnswerInput.value = "";
    questionAnswerSelect.hidden = false;
    questionAnswerSelect.required = true;
    const optionCount = type === "TRUE_FALSE" ? 2 : 5;
    const defaultOptions = type === "TRUE_FALSE" ? ["참", "거짓"] : ["", "", "", "", ""];

    const fields = [];
    for (let i = 0; i < optionCount; i += 1) {
        fields.push(`
            <label class="field">
                <span>선택지 ${i + 1}</span>
                <input
                    type="text"
                    class="question-option-input"
                    data-option-index="${i + 1}"
                    value="${defaultOptions[i]}"
                    placeholder="선택지 ${i + 1}"
                    ${type === "TRUE_FALSE" ? 'readonly' : 'required'}
                >
            </label>
        `);
    }

    questionOptionsContainer.innerHTML = fields.join("");
    renderQuestionAnswerOptions(type, defaultOptions);
};

const renderQuestionAnswerOptions = (type, options) => {
    if (!questionAnswerSelect) {
        return;
    }

    if (type === "SHORT_ANSWER") {
        questionAnswerSelect.innerHTML = "";
        return;
    }

    const choices = type === "TRUE_FALSE"
        ? options.map((option) => `<option value="${option}">${option}</option>`)
        : options.map((option, index) => `<option value="${index + 1}">${index + 1}번</option>`);
    questionAnswerSelect.innerHTML = choices.join("");
};

const collectQuestionOptions = () => {
    if (!questionTypeSelect) {
        return [];
    }

    const type = QUESTION_TYPE_VALUES[questionTypeSelect.value];
    if (type === "SHORT_ANSWER") {
        return [];
    }

    return Array.from(document.querySelectorAll(".question-option-input"))
        .map((input) => input.value.trim())
        .filter(Boolean);
};

const getQuestionAnswerValue = () => {
    if (!questionTypeSelect || !questionAnswerInput || !questionAnswerSelect) {
        return "";
    }

    const type = QUESTION_TYPE_VALUES[questionTypeSelect.value];
    if (type === "SHORT_ANSWER") {
        return questionAnswerInput.value.trim();
    }
    return questionAnswerSelect.value.trim();
};

const applyQuestionFilter = () => {
    if (!publicQuestionTableBody || !topicFilter) {
        return;
    }

    const selectedValue = topicFilter.value;
    Array.from(publicQuestionTableBody.querySelectorAll("tr[data-question-id]")).forEach((row) => {
        row.hidden = !(selectedValue === "all" || row.dataset.topicId === selectedValue);
    });
};

const updateQuestionRowStatus = (row, active) => {
    const statusBadge = row.querySelector(".table-badge");
    const actionButton = row.querySelector(".ghost-button[data-action='disable'], .ghost-button[data-action='enable']");

    if (!statusBadge || !actionButton) {
        return;
    }

    if (active) {
        statusBadge.textContent = "활성";
        statusBadge.classList.remove("is-inactive");
        statusBadge.classList.add("is-active");
        actionButton.textContent = "비활성화";
        actionButton.dataset.action = "disable";
    } else {
        statusBadge.textContent = "비활성";
        statusBadge.classList.remove("is-active");
        statusBadge.classList.add("is-inactive");
        actionButton.textContent = "활성화";
        actionButton.dataset.action = "enable";
    }
};

const fillQuestionDetail = async (row) => {
    if (!row) {
        return;
    }

    const questionId = row.dataset.questionId;
    if (!questionId) {
        return;
    }

    const detail = await request(`/admin/v1/questions/${questionId}`);
    selectedQuestionRow = row;
    selectedQuestionDetail = detail;

    row.dataset.answer = detail.answer;
    row.dataset.explanation = detail.explanation;
    row.dataset.options = JSON.stringify(detail.options);

    if (detailQuestion) detailQuestion.textContent = detail.content;
    if (detailType) detailType.textContent = QUESTION_TYPE_LABELS[detail.type] ?? detail.type;
    if (detailLevel) detailLevel.textContent = DIFFICULTY_LABELS[detail.difficulty] ?? detail.difficulty;
    if (detailTopic) detailTopic.textContent = detail.topicName;
    if (detailAnswer) detailAnswer.textContent = detail.answer;
    if (detailExplanation) {
        const optionsText = detail.options.length > 0 ? `선택지: ${detail.options.join(" / ")}\n\n` : "";
        detailExplanation.textContent = `${optionsText}${detail.explanation}`;
    }

    if (detailToggleStatusButton) {
        detailToggleStatusButton.dataset.action = detail.active ? "disable" : "enable";
        detailToggleStatusButton.textContent = detail.active ? "비활성화" : "활성화";
    }
};

const currentPage = window.location.pathname.split("/").pop() || "index.html";

if (!ensureAuthenticated()) {
    // redirected
} else {
    applyAdminIdentity();
}

if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const adminId = document.getElementById("adminId").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!adminId || !password) {
            setStatus("아이디와 비밀번호를 모두 입력해야 합니다.", "is-warning");
            return;
        }

        try {
            setStatus("로그인 중입니다.");
            const data = await request("/admin/auth/v1/login", {
                method: "POST",
                auth: false,
                body: {
                    username: adminId,
                    password
                }
            });
            setStoredSession({
                username: data.username,
                accessToken: data.accessToken,
                refreshToken: data.refreshToken
            });
            setStatus("로그인에 성공했습니다. 관리자 메인페이지로 이동합니다.", "is-success");
            window.setTimeout(() => {
                window.location.href = "./dashboard.html";
            }, 400);
        } catch (error) {
            setStatus(`로그인 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (adminLogoutButtons.length > 0) {
    adminLogoutButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await request("/admin/auth/v1/logout", {
                    method: "POST"
                });
            } catch (error) {
                console.error("관리자 로그아웃 요청 실패", error);
            } finally {
                clearAdminIdentity();
                window.location.href = "./index.html";
            }
        });
    });
}

if (currentPage === "dashboard.html" && ensureAuthenticated()) {
    loadMonitoringStats();
}

if (rangeButtons.length > 0) {
    rangeButtons.forEach((button) => {
        button.addEventListener("click", () => {
            rangeButtons.forEach((candidate) => candidate.classList.remove("is-active"));
            button.classList.add("is-active");
            currentMonitoringRange = button.dataset.range ?? "일별";
            loadMonitoringStats();
        });
    });
}

if (currentPage === "admin-create.html" && ensureAuthenticated()) {
    loadAdmins();
}

if (openAdminModalButton) {
    openAdminModalButton.addEventListener("click", () => toggleAdminModal(true));
}

if (closeAdminModalButton) {
    closeAdminModalButton.addEventListener("click", () => toggleAdminModal(false));
}

if (cancelAdminModalButton) {
    cancelAdminModalButton.addEventListener("click", () => toggleAdminModal(false));
}

if (adminCreateForm) {
    adminCreateForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const username = document.getElementById("newAdminId").value.trim();
        const password = document.getElementById("newAdminPassword").value.trim();

        if (!username || !password) {
            setAdminCreateStatus("아이디와 비밀번호를 모두 입력해야 합니다.", "is-warning");
            return;
        }

        try {
            const created = await request("/admin/v1/users", {
                method: "POST",
                body: { username, password }
            });
            toggleAdminModal(false);
            adminCreateForm.reset();
            await loadAdmins();
            setAdminCreateStatus(`관리자 계정 ${created.username} 이(가) 생성되었습니다.`, "is-success");
        } catch (error) {
            setAdminCreateStatus(`관리자 생성 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (adminTableBody) {
    adminTableBody.addEventListener("click", async (event) => {
        const target = event.target;
        if (!(target instanceof HTMLButtonElement)) {
            const appendRow = target instanceof HTMLElement ? target.closest("#appendAdminRow") : null;
            if (appendRow) {
                toggleAdminModal(true);
            }
            return;
        }

        const row = target.closest("tr[data-admin-id]");
        const action = target.dataset.action;
        if (!row || !action) {
            return;
        }

        const adminId = row.dataset.adminId;
        try {
            if (action === "delete") {
                await request(`/admin/v1/users/${adminId}`, { method: "DELETE" });
                await loadAdmins();
                setAdminCreateStatus("관리자 계정을 삭제했습니다.", "is-success");
                return;
            }

            const active = action === "enable";
            await request(`/admin/v1/users/${adminId}/status`, {
                method: "PATCH",
                body: { active }
            });
            await loadAdmins();
            setAdminCreateStatus(`관리자 계정을 ${active ? "활성화" : "비활성화"}했습니다.`, "is-success");
        } catch (error) {
            setAdminCreateStatus(`관리자 작업 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (currentPage === "questions-list.html" && ensureAuthenticated()) {
    updateQuestionOptionInputs();
    loadQuestionPage();
}

if (questionTypeSelect) {
    questionTypeSelect.addEventListener("change", updateQuestionOptionInputs);
}

if (openTopicModalButton) {
    openTopicModalButton.addEventListener("click", () => toggleTopicModal(true));
}

if (openQuestionModalButton) {
    openQuestionModalButton.addEventListener("click", () => toggleQuestionModal(true));
}

if (closeTopicModalButton) {
    closeTopicModalButton.addEventListener("click", () => toggleTopicModal(false));
}

if (cancelTopicModalButton) {
    cancelTopicModalButton.addEventListener("click", () => toggleTopicModal(false));
}

if (closeQuestionModalButton) {
    closeQuestionModalButton.addEventListener("click", () => toggleQuestionModal(false));
}

if (cancelQuestionModalButton) {
    cancelQuestionModalButton.addEventListener("click", () => toggleQuestionModal(false));
}

if (topicCreateForm) {
    topicCreateForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const name = document.getElementById("newTopicName").value.trim();
        if (!name) {
            return;
        }

        try {
            await request("/admin/v1/topics", {
                method: "POST",
                body: { name }
            });
            topicCreateForm.reset();
            toggleTopicModal(false);
            await loadTopics();
            setQuestionPageStatus("Topic을 등록했습니다.", "is-success");
        } catch (error) {
            setQuestionPageStatus(`Topic 등록 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (questionCreateForm) {
    questionCreateForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const content = document.getElementById("newQuestionText").value.trim();
        const type = QUESTION_TYPE_VALUES[document.getElementById("newQuestionType").value];
        const difficulty = DIFFICULTY_VALUES[document.getElementById("newQuestionLevel").value];
        const topicId = Number(document.getElementById("newQuestionTopic").value);
        const answer = getQuestionAnswerValue();
        const explanation = document.getElementById("newQuestionExplanation").value.trim();
        const options = collectQuestionOptions();

        if (!content || !type || !difficulty || !topicId || !answer || !explanation) {
            setQuestionPageStatus("문제 등록에 필요한 값을 모두 입력해야 합니다.", "is-warning");
            return;
        }

        try {
            await request("/admin/v1/questions", {
                method: "POST",
                body: {
                    topicId,
                    content,
                    type,
                    difficulty,
                    answer,
                    explanation,
                    options
                }
            });
            questionCreateForm.reset();
            updateQuestionOptionInputs();
            toggleQuestionModal(false);
            await Promise.all([loadTopics(), loadQuestions()]);
            setQuestionPageStatus("공용 문제를 등록했습니다.", "is-success");
        } catch (error) {
            setQuestionPageStatus(`공용 문제 등록 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (topicFilter) {
    topicFilter.addEventListener("change", async () => {
        const selectedText = topicFilter.options[topicFilter.selectedIndex]?.text ?? "전체";
        questionListDescription.textContent = `${selectedText} Topic 기준으로 등록된 공용 문제를 조회하고 관리합니다.`;
        try {
            await loadQuestions();
        } catch (error) {
            setQuestionPageStatus(`공용 문제 목록 조회 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (topicTableBody) {
    topicTableBody.addEventListener("click", async (event) => {
        const target = event.target;
        if (!(target instanceof HTMLButtonElement)) {
            const appendRow = target instanceof HTMLElement ? target.closest("#appendTopicRow") : null;
            if (appendRow) {
                toggleTopicModal(true);
            }
            return;
        }

        const row = target.closest("tr[data-topic-id]");
        if (!row || target.dataset.action !== "delete-topic") {
            return;
        }

        try {
            await request(`/admin/v1/topics/${row.dataset.topicId}`, { method: "DELETE" });
            await Promise.all([loadTopics(), loadQuestions()]);
            setQuestionPageStatus("Topic을 삭제했습니다.", "is-success");
        } catch (error) {
            setQuestionPageStatus(`Topic 삭제 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (publicQuestionTableBody) {
    publicQuestionTableBody.addEventListener("click", async (event) => {
        const target = event.target;
        const row = target instanceof HTMLElement ? target.closest("tr[data-question-id]") : null;
        const appendRow = target instanceof HTMLElement ? target.closest("#appendQuestionRow") : null;

        if (appendRow) {
            toggleQuestionModal(true);
            return;
        }

        if (target instanceof HTMLInputElement && target.classList.contains("row-checkbox")) {
            return;
        }

        if (!(target instanceof HTMLButtonElement)) {
            if (row) {
                try {
                    await fillQuestionDetail(row);
                    toggleQuestionDetailModal(true);
                } catch (error) {
                    setQuestionPageStatus(`문제 상세 조회 실패: ${getErrorMessage(error)}`, "is-warning");
                }
            }
            return;
        }

        if (!row) {
            return;
        }

        const questionId = row.dataset.questionId;
        const action = target.dataset.action;
        try {
            if (action === "delete") {
                await request(`/admin/v1/questions/${questionId}`, { method: "DELETE" });
                await Promise.all([loadTopics(), loadQuestions()]);
                setQuestionPageStatus("공용 문제를 삭제했습니다.", "is-success");
                return;
            }

            const active = action === "enable";
            await request(`/admin/v1/questions/${questionId}/status`, {
                method: "PATCH",
                body: { active }
            });
            updateQuestionRowStatus(row, active);
            setQuestionPageStatus(`공용 문제를 ${active ? "활성화" : "비활성화"}했습니다.`, "is-success");
        } catch (error) {
            setQuestionPageStatus(`공용 문제 작업 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (tabButtons.length > 0 && tabPanels.length > 0) {
    tabButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const tab = button.dataset.tab;
            tabButtons.forEach((candidate) => candidate.classList.remove("is-active"));
            tabPanels.forEach((panel) => panel.classList.remove("is-active"));
            button.classList.add("is-active");
            const activePanel = document.querySelector(`.tab-panel[data-panel="${tab}"]`);
            activePanel?.classList.add("is-active");
        });
    });
}

if (closeQuestionDetailModalButton) {
    closeQuestionDetailModalButton.addEventListener("click", () => toggleQuestionDetailModal(false));
}

if (cancelQuestionDetailModalButton) {
    cancelQuestionDetailModalButton.addEventListener("click", () => toggleQuestionDetailModal(false));
}

if (detailToggleStatusButton) {
    detailToggleStatusButton.addEventListener("click", async () => {
        if (!selectedQuestionRow || !selectedQuestionDetail) {
            return;
        }

        const active = detailToggleStatusButton.dataset.action === "enable";
        try {
            await request(`/admin/v1/questions/${selectedQuestionDetail.questionId}/status`, {
                method: "PATCH",
                body: { active }
            });
            await fillQuestionDetail(selectedQuestionRow);
            updateQuestionRowStatus(selectedQuestionRow, active);
            setQuestionPageStatus(`공용 문제를 ${active ? "활성화" : "비활성화"}했습니다.`, "is-success");
        } catch (error) {
            setQuestionPageStatus(`상태 변경 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (detailDeleteButton) {
    detailDeleteButton.addEventListener("click", async () => {
        if (!selectedQuestionDetail) {
            return;
        }

        try {
            await request(`/admin/v1/questions/${selectedQuestionDetail.questionId}`, { method: "DELETE" });
            toggleQuestionDetailModal(false);
            selectedQuestionRow = null;
            selectedQuestionDetail = null;
            await Promise.all([loadTopics(), loadQuestions()]);
            setQuestionPageStatus("공용 문제를 삭제했습니다.", "is-success");
        } catch (error) {
            setQuestionPageStatus(`삭제 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}

if (selectAllQuestions) {
    selectAllQuestions.addEventListener("change", () => {
        getVisibleQuestionRows().forEach((row) => {
            const checkbox = row.querySelector(".row-checkbox");
            if (checkbox) {
                checkbox.checked = selectAllQuestions.checked;
            }
        });
    });
}

const changeBulkQuestionStatuses = async (active) => {
    const questionIds = getCheckedQuestionRows()
        .map((row) => Number(row.dataset.questionId));

    if (questionIds.length === 0) {
        setQuestionPageStatus("먼저 공용 문제를 선택해야 합니다.", "is-warning");
        return;
    }

    try {
        await request("/admin/v1/questions/status", {
            method: "PATCH",
            body: { questionIds, active }
        });
        await loadQuestions();
        setQuestionPageStatus(`선택한 공용 문제를 ${active ? "활성화" : "비활성화"}했습니다.`, "is-success");
    } catch (error) {
        setQuestionPageStatus(`일괄 상태 변경 실패: ${getErrorMessage(error)}`, "is-warning");
    }
};

if (bulkDisableButton) {
    bulkDisableButton.addEventListener("click", () => changeBulkQuestionStatuses(false));
}

if (bulkEnableButton) {
    bulkEnableButton.addEventListener("click", () => changeBulkQuestionStatuses(true));
}

if (bulkDeleteButton) {
    bulkDeleteButton.addEventListener("click", async () => {
        const questionIds = getCheckedQuestionRows()
            .map((row) => Number(row.dataset.questionId));

        if (questionIds.length === 0) {
            setQuestionPageStatus("먼저 공용 문제를 선택해야 합니다.", "is-warning");
            return;
        }

        try {
            await request("/admin/v1/questions", {
                method: "DELETE",
                body: { questionIds }
            });
            await Promise.all([loadTopics(), loadQuestions()]);
            setQuestionPageStatus("선택한 공용 문제를 삭제했습니다.", "is-success");
        } catch (error) {
            setQuestionPageStatus(`일괄 삭제 실패: ${getErrorMessage(error)}`, "is-warning");
        }
    });
}
