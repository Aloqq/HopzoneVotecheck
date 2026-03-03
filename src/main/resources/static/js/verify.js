const form = document.getElementById('verifyForm');
const resultCard = document.getElementById('resultCard');
const resultVoted = document.getElementById('resultVoted');
const resultVoteTime = document.getElementById('resultVoteTime');
const resultServerTime = document.getElementById('resultServerTime');
const resultRawJson = document.getElementById('resultRawJson');
const successBlock = document.getElementById('successBlock');
const failBlock = document.getElementById('failBlock');
const screenshotIpInput = document.getElementById('screenshotIp');
const screenshotVoteInput = document.getElementById('screenshotVote');
const previewScreenshotIp = document.getElementById('previewScreenshotIp');
const previewScreenshotVote = document.getElementById('previewScreenshotVote');

const appBaseUrl = (window.APP_BASE_URL || '').replace(/\/$/, '');

const STORAGE_KEY = 'hopzone_verify_user';

function loadSavedData() {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (saved) {
            const data = JSON.parse(saved);
            if (data.hopzoneAccountId) document.getElementById('hopzoneAccountId').value = data.hopzoneAccountId;
            if (data.gameNickname) document.getElementById('gameNickname').value = data.gameNickname;
            if (data.telegram) document.getElementById('telegram').value = data.telegram;
            if (data.discord) document.getElementById('discord').value = data.discord;
            if (data.email) document.getElementById('email').value = data.email;
        }
    } catch (e) { /* ignore */ }
}

function saveUserData() {
    const data = {
        hopzoneAccountId: document.getElementById('hopzoneAccountId').value?.trim() || '',
        gameNickname: document.getElementById('gameNickname').value?.trim() || '',
        telegram: document.getElementById('telegram').value?.trim() || '',
        discord: document.getElementById('discord').value?.trim() || '',
        email: document.getElementById('email').value?.trim() || ''
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

document.addEventListener('DOMContentLoaded', loadSavedData);

function clearPreview(input, previewEl, clearBtn) {
    if (previewEl.src && previewEl.src.startsWith('blob:')) {
        URL.revokeObjectURL(previewEl.src);
    }
    previewEl.classList.add('hidden');
    previewEl.removeAttribute('src');
    input.value = '';
    if (clearBtn) clearBtn.classList.add('hidden');
}

function setPreview(input, previewEl, clearBtn) {
    if (!input.files || !input.files[0]) {
        clearPreview(input, previewEl, clearBtn);
        return;
    }
    if (previewEl.src && previewEl.src.startsWith('blob:')) {
        URL.revokeObjectURL(previewEl.src);
    }
    previewEl.src = URL.createObjectURL(input.files[0]);
    previewEl.classList.remove('hidden');
    if (clearBtn) clearBtn.classList.remove('hidden');
}

function attachPasteZone(zone) {
    const targetId = zone.dataset.target;
    const input = document.getElementById(targetId);
    const previewEl = targetId === 'screenshotIp' ? previewScreenshotIp : previewScreenshotVote;
    const clearBtn = targetId === 'screenshotIp' ? document.getElementById('clearScreenshotIp') : document.getElementById('clearScreenshotVote');

    zone.addEventListener('paste', (event) => {
        const items = event.clipboardData?.items || [];
        for (const item of items) {
            if (item.type.startsWith('image/')) {
                const file = item.getAsFile();
                const ext = (file.type.split('/')[1] || 'png').replace('jpeg', 'jpg');
                const pastedFile = new File([file], `pasted-${Date.now()}.${ext}`, { type: file.type });
                const transfer = new DataTransfer();
                transfer.items.add(pastedFile);
                input.files = transfer.files;
                setPreview(input, previewEl, clearBtn);
                event.preventDefault();
                break;
            }
        }
    });
}

document.querySelectorAll('.paste-zone').forEach(attachPasteZone);
screenshotIpInput.addEventListener('change', () => setPreview(screenshotIpInput, previewScreenshotIp, document.getElementById('clearScreenshotIp')));
screenshotVoteInput.addEventListener('change', () => setPreview(screenshotVoteInput, previewScreenshotVote, document.getElementById('clearScreenshotVote')));

document.getElementById('clearScreenshotIp').addEventListener('click', () => {
    clearPreview(screenshotIpInput, previewScreenshotIp, document.getElementById('clearScreenshotIp'));
});
document.getElementById('clearScreenshotVote').addEventListener('click', () => {
    clearPreview(screenshotVoteInput, previewScreenshotVote, document.getElementById('clearScreenshotVote'));
});

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    const captcha = document.querySelector('[name="g-recaptcha-response"]')?.value || '';
    formData.set('g-recaptcha-response', captcha);

    try {
        const response = await fetch('/api/check', { method: 'POST', body: formData });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || 'Request failed');
        }

        saveUserData();

        resultVoted.textContent = String(data.voted);
        resultVoteTime.textContent = data.voteTime || '-';
        resultServerTime.textContent = data.hopzoneServerTime || '-';
        resultRawJson.value = data.rawJson || '';

        if (data.voted) {
            successBlock.classList.remove('hidden');
            failBlock.classList.add('hidden');
            resultCard.classList.remove('hidden');
        } else {
            successBlock.classList.add('hidden');
            failBlock.classList.remove('hidden');
            resultCard.classList.remove('hidden');
            const reportAbs = appBaseUrl ? appBaseUrl + data.reportUrl : data.reportUrl;
            const reportLink = document.getElementById('reportLink');
            reportLink.href = reportAbs;
            reportLink.textContent = reportAbs;
            document.getElementById('copyReportLinkBtn').onclick = () => {
                navigator.clipboard.writeText(reportAbs);
                alert('Ссылка скопирована');
            };
        }
        if (window.grecaptcha) {
            window.grecaptcha.reset();
        }
    } catch (error) {
        alert(error.message);
    }
});
