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
const documentFileInput = document.getElementById("documentFileInput");
const documentFileName = document.getElementById("documentFileName");
const documentUploadStatus = document.getElementById("documentUploadStatus");
const documentProgressSummary = document.getElementById("documentProgressSummary");
const documentProgressSteps = document.querySelectorAll(".document-progress-step");
const examStartForms = document.querySelectorAll("[data-exam-start-form]");
const examEmptyState = document.getElementById("examEmptyState");
const examActiveState = document.getElementById("examActiveState");
const examActiveTitle = document.getElementById("examActiveTitle");
const examActiveMeta = document.getElementById("examActiveMeta");
const resumeExamButton = document.getElementById("resumeExamButton");
const examEntryGrid = document.getElementById("examEntryGrid");
const examRoomContent = document.querySelectorAll("[data-exam-room-content]");
const examRoomEmptyState = document.getElementById("examRoomEmptyState");
const examRoomTitle = document.getElementById("examRoomTitle");
const examRemainingTime = document.getElementById("examRemainingTime");
const examRoomSourceType = document.getElementById("examRoomSourceType");
const examRoomQuestionCount = document.getElementById("examRoomQuestionCount");
const examRoomDuration = document.getElementById("examRoomDuration");
const examAnsweredCount = document.getElementById("examAnsweredCount");
const examAnswerButtons = document.querySelectorAll("[data-answer-confirm]");
const examQuestionCards = document.querySelectorAll(".exam-question-card");
const submitExamButton = document.getElementById("submitExamButton");
const historyItems = document.querySelectorAll("[data-history-item]");
const historyDetailTitle = document.getElementById("historyDetailTitle");
const historyDetailMeta = document.getElementById("historyDetailMeta");
const historyDetailList = document.getElementById("historyDetailList");

const USER_SESSION_KEY = "tmk_user_username";
const EXAM_SESSION_KEY = "tmk_user_exam_session";
const EXAM_ANSWERS_KEY = "tmk_user_exam_answers";
const HISTORY_DETAIL_KEY = "tmk_user_history_detail";

let pendingRegisterPayload = null;
let examCountdownTimer = null;

const resetStatus = () => {
    if (!userLoginStatus) {
        return;
    }
    userLoginStatus.classList.remove("is-success", "is-warning");
    userLoginStatus.textContent = "사용자 인증 API 연동 전입니다. 현재는 진입 흐름만 확인할 수 있습니다.";
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
    const username = window.sessionStorage.getItem(USER_SESSION_KEY) || "user";
    userNameDisplays.forEach((node) => {
        node.textContent = username;
    });
};

const formatRemainingTime = (remainingMs) => {
    const totalSeconds = Math.max(0, Math.floor(remainingMs / 1000));
    const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
    const seconds = String(totalSeconds % 60).padStart(2, "0");
    return `${minutes}:${seconds}`;
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
    if (!examAnsweredCount || examQuestionCards.length === 0) {
        return;
    }

    const answers = getExamAnswers();
    const answered = Object.keys(answers).filter((key) => answers[key]).length;
    examAnsweredCount.textContent = `${answered} / ${examQuestionCards.length}`;
};

const applySavedExamAnswers = () => {
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

const renderExamOverview = () => {
    const examSession = getActiveExamSession();
    if (!examEmptyState || !examEntryGrid) {
        return;
    }

    if (!examSession) {
        examEmptyState.hidden = false;
        if (examActiveState) {
            examActiveState.hidden = true;
        }
        examEntryGrid.hidden = false;
        return;
    }

    examEmptyState.hidden = true;
    examEntryGrid.hidden = true;

    if (examActiveState && examActiveTitle && examActiveMeta) {
        examActiveState.hidden = false;
        examActiveTitle.textContent = examSession.title;
        examActiveMeta.textContent = `${examSession.sourceLabel} · ${examSession.questionCount}문제 · 남은 시간 ${formatRemainingTime(examSession.expiresAt - Date.now())}`;
    }
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

if (loginTriggers.length > 0) {
    loginTriggers.forEach((button) => {
        button.addEventListener("click", () => {
            window.location.href = "./login.html";
        });
    });
}

if (userLoginForm && userLoginStatus) {
    userLoginForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const userId = document.getElementById("userLoginId").value.trim();
        const password = document.getElementById("userLoginPassword").value.trim();

        userLoginStatus.classList.remove("is-success", "is-warning");

        if (!userId || !password) {
            userLoginStatus.textContent = "아이디와 비밀번호를 모두 입력해야 합니다.";
            userLoginStatus.classList.add("is-warning");
            return;
        }

        window.sessionStorage.setItem(USER_SESSION_KEY, userId);
        userLoginStatus.textContent = "입력 검증이 완료되었습니다. 사용자 홈으로 이동합니다.";
        userLoginStatus.classList.add("is-success");
        window.setTimeout(() => {
            window.location.href = "./home.html";
        }, 400);
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
    confirmRegisterButton.addEventListener("click", () => {
        if (!pendingRegisterPayload) {
            toggleRegisterConfirmModal(false);
            return;
        }

        userLoginStatus.classList.remove("is-warning");
        userLoginStatus.classList.add("is-success");
        userLoginStatus.textContent = "정책 확인이 완료되었습니다. API 연동 후 이 시점에 회원가입 요청이 전송됩니다.";
        userRegisterForm.reset();
        pendingRegisterPayload = null;
        toggleRegisterConfirmModal(false);
        setAuthMode("login");
    });
}

if (userNameDisplays.length > 0) {
    applyUserIdentity();
}

if (userLogoutButtons.length > 0) {
    userLogoutButtons.forEach((button) => {
        button.addEventListener("click", () => {
            window.sessionStorage.removeItem(USER_SESSION_KEY);
            window.location.href = "./index.html";
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

        userLoginStatus.textContent = "아이디 확인이 완료되었다고 가정합니다. API 연동 후 이 시점에 존재 여부를 검증합니다.";
        userLoginStatus.classList.add("is-success");
    });
}

if (resetPasswordForm && userLoginStatus) {
    resetPasswordForm.addEventListener("submit", (event) => {
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

        userLoginStatus.textContent = "비밀번호 재설정 입력 검증이 완료되었습니다. API 연동 후 이 시점에 재설정 요청이 전송됩니다.";
        userLoginStatus.classList.add("is-success");
        setAuthMode("login");
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
    } else {
        documentProgressSummary.textContent = "아직 업로드가 시작되지 않았습니다.";
    }
};

if (documentFileInput && documentFileName) {
    documentFileInput.addEventListener("change", () => {
        const selectedFile = documentFileInput.files && documentFileInput.files[0];
        documentFileName.textContent = selectedFile ? selectedFile.name : "선택된 파일이 없습니다.";
    });
}

if (documentUploadForm && documentUploadStatus) {
    documentUploadForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const selectedFile = documentFileInput && documentFileInput.files ? documentFileInput.files[0] : null;

        documentUploadStatus.classList.remove("is-success", "is-warning");

        if (!selectedFile) {
            documentUploadStatus.textContent = "먼저 위 영역에서 업로드할 문서를 선택해야 합니다.";
            documentUploadStatus.classList.add("is-warning");
            return;
        }

        setDocumentProgress("upload");
        documentUploadStatus.textContent = "문서 업로드를 시작했습니다.";
        documentUploadStatus.classList.add("is-success");

        window.setTimeout(() => {
            setDocumentProgress("generating");
            documentUploadStatus.textContent = "업로드가 완료되어 문제를 생성하고 있습니다.";
        }, 800);

        window.setTimeout(() => {
            setDocumentProgress("completed");
            documentUploadStatus.textContent = "문제 생성이 완료되었습니다. API 연동 후 이 시점에 생성 결과와 다음 액션이 연결됩니다.";
        }, 1800);
    });
}

if (examStartForms.length > 0) {
    examStartForms.forEach((form) => {
        form.addEventListener("submit", (event) => {
            event.preventDefault();

            const activeExam = getActiveExamSession();
            if (activeExam) {
                renderExamOverview();
                return;
            }

            const sourceType = form.dataset.examSource;
            const sourceLabel = form.elements.sourceLabel.value.trim();
            const questionCount = Number(form.elements.questionCount.value);
            const durationMinutes = Number(form.elements.durationMinutes.value);

            if (!sourceLabel || !questionCount || !durationMinutes) {
                return;
            }

            const examSession = {
                sourceType,
                sourceLabel,
                questionCount,
                durationMinutes,
                title: sourceType === "PUBLIC" ? "공용문제 시험" : "개인문제 시험",
                startedAt: Date.now(),
                expiresAt: Date.now() + (durationMinutes * 60 * 1000)
            };

            window.sessionStorage.setItem(EXAM_SESSION_KEY, JSON.stringify(examSession));
            window.sessionStorage.setItem(EXAM_ANSWERS_KEY, JSON.stringify({}));
            window.location.href = "./exam-room.html";
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
        const examSession = getActiveExamSession();
        if (!examSession) {
            renderExamOverview();
            return;
        }

        examActiveMeta.textContent = `${examSession.sourceLabel} · ${examSession.questionCount}문제 · 남은 시간 ${formatRemainingTime(examSession.expiresAt - Date.now())}`;
    }, 1000);
}

if (examAnswerButtons.length > 0) {
    examAnswerButtons.forEach((button) => {
        button.addEventListener("click", () => {
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

            const answers = getExamAnswers();
            answers[questionId] = answerValue;
            setExamAnswers(answers);

            if (badge) {
                badge.textContent = "입력 완료";
                badge.classList.add("is-confirmed");
            }

            updateExamAnsweredCount();
        });
    });
}

if (submitExamButton) {
    submitExamButton.addEventListener("click", () => {
        clearExamSession();
        window.location.href = "./exams.html";
    });
}

if (historyItems.length > 0) {
    historyItems.forEach((item) => {
        item.addEventListener("click", () => {
            let questions = [];
            try {
                questions = JSON.parse(item.dataset.historyQuestions || "[]");
            } catch {
                questions = [];
            }

            window.sessionStorage.setItem(HISTORY_DETAIL_KEY, JSON.stringify({
                title: item.dataset.historyTitle || "",
                meta: item.dataset.historyMeta || "",
                questions
            }));
            window.location.href = "./history-detail.html";
        });
    });
}

renderExamOverview();
renderExamRoom();
renderStoredHistoryDetail();
