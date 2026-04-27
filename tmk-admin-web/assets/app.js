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

let selectedQuestionRow = null;

const setStatus = (message, kind) => {
    statusCard.textContent = message;
    statusCard.classList.remove("is-success", "is-warning");

    if (kind) {
        statusCard.classList.add(kind);
    }
};

const toggleQuestionDetailModal = (isOpen) => {
    if (!questionDetailModal) {
        return;
    }

    questionDetailModal.hidden = !isOpen;
};

const toggleTopicModal = (isOpen) => {
    if (!topicModalBackdrop) {
        return;
    }

    topicModalBackdrop.hidden = !isOpen;
};

const toggleQuestionModal = (isOpen) => {
    if (!questionModalBackdrop) {
        return;
    }

    questionModalBackdrop.hidden = !isOpen;
};

const fillQuestionDetail = (row) => {
    if (!row) {
        return;
    }

    selectedQuestionRow = row;

    if (detailQuestion) detailQuestion.textContent = row.dataset.question ?? "-";
    if (detailType) detailType.textContent = row.dataset.type ?? "-";
    if (detailLevel) detailLevel.textContent = row.dataset.level ?? "-";
    if (detailTopic) detailTopic.textContent = row.dataset.topicName ?? "-";
    if (detailAnswer) detailAnswer.textContent = row.dataset.answer ?? "-";
    if (detailExplanation) detailExplanation.textContent = row.dataset.explanation ?? "-";

    if (detailToggleStatusButton) {
        const statusBadge = row.querySelector(".table-badge");
        const isActive = statusBadge?.classList.contains("is-active");
        detailToggleStatusButton.textContent = isActive ? "비활성화" : "활성화";
        detailToggleStatusButton.dataset.action = isActive ? "disable" : "enable";
    }
};

const updateQuestionRowStatus = (row, action) => {
    const statusBadge = row.querySelector(".table-badge");
    const actionButton = row.querySelector(".ghost-button[data-action='disable'], .ghost-button[data-action='enable']");

    if (!statusBadge || !actionButton) {
        return;
    }

    if (action === "disable") {
        statusBadge.textContent = "비활성";
        statusBadge.classList.remove("is-active");
        statusBadge.classList.add("is-inactive");
        actionButton.textContent = "활성화";
        actionButton.dataset.action = "enable";
    } else if (action === "enable") {
        statusBadge.textContent = "활성";
        statusBadge.classList.remove("is-inactive");
        statusBadge.classList.add("is-active");
        actionButton.textContent = "비활성화";
        actionButton.dataset.action = "disable";
    }
};

const getVisibleQuestionRows = () => {
    if (!publicQuestionTableBody) {
        return [];
    }

    return Array.from(publicQuestionTableBody.children).filter((row) => !row.hidden);
};

const getCheckedQuestionRows = () => {
    return getVisibleQuestionRows().filter((row) => row.querySelector(".row-checkbox")?.checked);
};

if (loginForm) {
    loginForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const adminId = document.getElementById("adminId").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!adminId || !password) {
            setStatus("아이디와 비밀번호를 모두 입력해야 합니다.", "is-warning");
            return;
        }

        setStatus("입력 검증이 완료되었습니다. 관리자 메인페이지로 이동합니다.", "is-success");
        window.setTimeout(() => {
            window.location.href = "./dashboard.html";
        }, 500);
    });
}

if (adminCreateForm && adminCreateStatus) {
    adminCreateForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const newAdminId = document.getElementById("newAdminId").value.trim();
        const newAdminPassword = document.getElementById("newAdminPassword").value.trim();

        adminCreateStatus.classList.remove("is-success", "is-warning");

        if (!newAdminId || !newAdminPassword) {
            adminCreateStatus.textContent = "아이디와 비밀번호를 모두 입력해야 합니다.";
            adminCreateStatus.classList.add("is-warning");
            return;
        }

        if (adminTableBody) {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${newAdminId}</td>
                <td><span class="table-badge is-active">활성</span></td>
                <td>2026-04-27</td>
                <td class="table-actions">
                    <button type="button" class="ghost-button" data-action="disable">비활성화</button>
                    <button type="button" class="ghost-button is-danger" data-action="delete">삭제</button>
                </td>
            `;
            adminTableBody.insertBefore(row, appendAdminRow);
        }

        adminCreateStatus.textContent = "관리자 계정이 목록에 추가되었습니다. API 연동 후 이 시점에 실제 생성 요청이 전송됩니다.";
        adminCreateStatus.classList.add("is-success");
        adminCreateForm.reset();
        if (adminModalBackdrop) {
            adminModalBackdrop.hidden = true;
        }
    });
}

const toggleAdminModal = (isOpen) => {
    if (!adminModalBackdrop) {
        return;
    }

    adminModalBackdrop.hidden = !isOpen;
};

if (openAdminModalButton) {
    openAdminModalButton.addEventListener("click", () => toggleAdminModal(true));
}

if (appendAdminRow) {
    appendAdminRow.addEventListener("click", () => toggleAdminModal(true));
}

if (openTopicModalButton) {
    openTopicModalButton.addEventListener("click", () => toggleTopicModal(true));
}

if (openQuestionModalButton) {
    openQuestionModalButton.addEventListener("click", () => toggleQuestionModal(true));
}

if (appendQuestionRow) {
    appendQuestionRow.addEventListener("click", () => toggleQuestionModal(true));
}

if (appendTopicRow) {
    appendTopicRow.addEventListener("click", () => toggleTopicModal(true));
}

if (closeAdminModalButton) {
    closeAdminModalButton.addEventListener("click", () => toggleAdminModal(false));
}

if (cancelAdminModalButton) {
    cancelAdminModalButton.addEventListener("click", () => toggleAdminModal(false));
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

if (adminTableBody && adminCreateStatus) {
    adminTableBody.addEventListener("click", (event) => {
        const target = event.target;

        if (!(target instanceof HTMLButtonElement)) {
            return;
        }

        const action = target.dataset.action;
        const row = target.closest("tr");

        if (!row || !action) {
            return;
        }

        const statusBadge = row.querySelector(".table-badge");

        if (action === "delete") {
            row.remove();
            adminCreateStatus.textContent = "관리자 계정이 목록에서 삭제되었습니다. API 연동 후 이 시점에 삭제 요청이 전송됩니다.";
            adminCreateStatus.classList.remove("is-warning");
            adminCreateStatus.classList.add("is-success");
            return;
        }

        if (!statusBadge) {
            return;
        }

        if (action === "disable") {
            statusBadge.textContent = "비활성";
            statusBadge.classList.remove("is-active");
            statusBadge.classList.add("is-inactive");
            target.textContent = "활성화";
            target.dataset.action = "enable";
            adminCreateStatus.textContent = "관리자 계정이 비활성화되었습니다. API 연동 후 이 시점에 비활성화 요청이 전송됩니다.";
        } else if (action === "enable") {
            statusBadge.textContent = "활성";
            statusBadge.classList.remove("is-inactive");
            statusBadge.classList.add("is-active");
            target.textContent = "비활성화";
            target.dataset.action = "disable";
            adminCreateStatus.textContent = "관리자 계정이 활성화되었습니다. API 연동 후 이 시점에 활성화 요청이 전송됩니다.";
        }

        adminCreateStatus.classList.remove("is-warning");
        adminCreateStatus.classList.add("is-success");
    });
}

if (rangeButtons.length > 0) {
    rangeButtons.forEach((button) => {
        button.addEventListener("click", () => {
            rangeButtons.forEach((candidate) => candidate.classList.remove("is-active"));
            button.classList.add("is-active");

            if (monitoringDescription) {
                const range = button.dataset.range ?? button.textContent?.trim() ?? "일별";
                monitoringDescription.textContent = `사용자 접근과 학습 활동을 ${range} 기준으로 확인하는 관리자 메인페이지입니다.`;
            }
        });
    });
}

if (topicFilter && questionListDescription) {
    topicFilter.addEventListener("change", () => {
        const selectedText = topicFilter.options[topicFilter.selectedIndex]?.text ?? "전체";
        questionListDescription.textContent = `${selectedText} Topic 기준으로 등록된 공용 문제를 조회하고 관리합니다.`;

        if (publicQuestionTableBody) {
            const selectedValue = topicFilter.value;
            Array.from(publicQuestionTableBody.children).forEach((row) => {
                const rowTopic = row.getAttribute("data-topic");
                row.hidden = !(selectedValue === "all" || rowTopic === selectedValue);
            });
        }
    });
}

if (topicCreateForm && topicFilter) {
    topicCreateForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const newTopicNameInput = document.getElementById("newTopicName");
        const newTopicName = newTopicNameInput?.value.trim();

        if (!newTopicName) {
            return;
        }

        const normalizedValue = newTopicName.toLowerCase().replace(/\s+/g, "-");
        const exists = Array.from(topicFilter.options).some((option) => option.value === normalizedValue);

        if (!exists) {
            const option = document.createElement("option");
            option.value = normalizedValue;
            option.textContent = newTopicName;
            topicFilter.append(option);

            const questionTopicSelect = document.getElementById("newQuestionTopic");
            if (questionTopicSelect) {
                const questionOption = document.createElement("option");
                questionOption.value = normalizedValue;
                questionOption.textContent = newTopicName;
                questionTopicSelect.append(questionOption);
            }

            if (topicTableBody) {
                const row = document.createElement("tr");
                row.dataset.topicValue = normalizedValue;
                row.innerHTML = `
                    <td>${newTopicName}</td>
                    <td>0</td>
                    <td>2026-04-27</td>
                    <td class="table-actions">
                        <button type="button" class="ghost-button is-danger" data-action="delete-topic">삭제</button>
                    </td>
                `;
                topicTableBody.prepend(row);
            }
        }

        topicCreateForm.reset();
        toggleTopicModal(false);
    });
}

if (questionCreateForm && publicQuestionTableBody) {
    questionCreateForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const text = document.getElementById("newQuestionText")?.value.trim();
        const type = document.getElementById("newQuestionType")?.value;
        const level = document.getElementById("newQuestionLevel")?.value;
        const topicValue = document.getElementById("newQuestionTopic")?.value;
        const topicText = document.getElementById("newQuestionTopic")?.selectedOptions[0]?.textContent ?? topicValue;
        const answer = document.getElementById("newQuestionAnswer")?.value.trim();
        const explanation = document.getElementById("newQuestionExplanation")?.value.trim();

        if (!text || !type || !level || !topicValue || !answer || !explanation) {
            return;
        }

        const row = document.createElement("tr");
        row.dataset.topic = topicValue;
        row.dataset.question = text;
        row.dataset.type = type;
        row.dataset.level = level;
        row.dataset.topicName = topicText;
        row.dataset.answer = answer;
        row.dataset.explanation = explanation;
        row.innerHTML = `
            <td><input type="checkbox" class="row-checkbox" aria-label="문제 선택"></td>
            <td>${text}</td>
            <td>${type}</td>
            <td>${level}</td>
            <td>${topicText}</td>
            <td><span class="table-badge is-active">활성</span></td>
            <td>2026-04-27</td>
            <td class="table-actions">
                <button type="button" class="ghost-button" data-action="disable">비활성화</button>
                <button type="button" class="ghost-button is-danger" data-action="delete">삭제</button>
            </td>
        `;
        publicQuestionTableBody.prepend(row);

        if (topicFilter.value !== "all" && topicFilter.value !== topicValue) {
            row.hidden = true;
        }
        questionCreateForm.reset();
        toggleQuestionModal(false);
    });
}

if (publicQuestionTableBody) {
    publicQuestionTableBody.addEventListener("click", (event) => {
        const target = event.target;
        const row = target instanceof HTMLElement ? target.closest("tr") : null;

        if (target instanceof HTMLInputElement && target.classList.contains("row-checkbox")) {
            return;
        }

        if (!(target instanceof HTMLButtonElement)) {
            if (row) {
                fillQuestionDetail(row);
                toggleQuestionDetailModal(true);
            }
            return;
        }

        const action = target.dataset.action;

        if (!row || !action) {
            return;
        }

        const statusBadge = row.querySelector(".table-badge");

        if (action === "delete") {
            row.remove();
            return;
        }

        if (!statusBadge) {
            return;
        }

        updateQuestionRowStatus(row, action);
    });
}

if (topicTableBody) {
    topicTableBody.addEventListener("click", (event) => {
        const target = event.target;

        if (!(target instanceof HTMLButtonElement)) {
            return;
        }

        const row = target.closest("tr");
        const action = target.dataset.action;

        if (!row || action !== "delete-topic") {
            return;
        }

        const topicValue = row.dataset.topicValue;
        row.remove();

        if (topicValue && topicFilter) {
            Array.from(topicFilter.options).forEach((option) => {
                if (option.value === topicValue) {
                    option.remove();
                }
            });
        }

        const questionTopicSelect = document.getElementById("newQuestionTopic");
        if (topicValue && questionTopicSelect) {
            Array.from(questionTopicSelect.options).forEach((option) => {
                if (option.value === topicValue) {
                    option.remove();
                }
            });
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
    detailToggleStatusButton.addEventListener("click", () => {
        if (!selectedQuestionRow) {
            return;
        }

        const action = detailToggleStatusButton.dataset.action;
        if (!action) {
            return;
        }

        updateQuestionRowStatus(selectedQuestionRow, action);
        fillQuestionDetail(selectedQuestionRow);
    });
}

if (detailDeleteButton) {
    detailDeleteButton.addEventListener("click", () => {
        if (!selectedQuestionRow) {
            return;
        }

        selectedQuestionRow.remove();
        selectedQuestionRow = null;
        syncQuestionEmptyState();
        toggleQuestionDetailModal(false);
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

if (bulkDisableButton) {
    bulkDisableButton.addEventListener("click", () => {
        getCheckedQuestionRows().forEach((row) => updateQuestionRowStatus(row, "disable"));
    });
}

if (bulkEnableButton) {
    bulkEnableButton.addEventListener("click", () => {
        getCheckedQuestionRows().forEach((row) => updateQuestionRowStatus(row, "enable"));
    });
}

if (bulkDeleteButton) {
    bulkDeleteButton.addEventListener("click", () => {
        getCheckedQuestionRows().forEach((row) => row.remove());
    });
}
