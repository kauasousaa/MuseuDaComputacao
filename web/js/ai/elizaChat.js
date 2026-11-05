import { apiClient } from '../core/apiClient.js';
import { createElement, prefersReducedMotion, wait } from '../core/dom.js';

const greeting = 'OLÁ. EU SOU A ELIZA (MIT, 1966). COMO VOCÊ ESTÁ SE SENTINDO HOJE?';
const teletypeDelayMs = 22;

export function initElizaChat() {
  const log = document.getElementById('elizaLog');
  const form = document.getElementById('elizaForm');
  const input = document.getElementById('elizaInput');
  let isBusy = false;

  appendElizaLine(log, greeting);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message || isBusy) {
      return;
    }
    isBusy = true;
    input.value = '';
    appendLine(log, message, 'fromVisitor');
    try {
      const { reply } = await apiClient.post('/api/ai/eliza', { message });
      await appendElizaLine(log, reply.toUpperCase());
    } catch (error) {
      appendLine(log, `?ERRO: ${error.message}`, 'fromEliza');
    } finally {
      isBusy = false;
      input.focus();
    }
  });
}

function appendLine(log, text, className) {
  const line = createElement('p', { className: `elizaLine ${className}`, text });
  log.append(line);
  log.scrollTop = log.scrollHeight;
  return line;
}

async function appendElizaLine(log, text) {
  if (prefersReducedMotion()) {
    appendLine(log, text, 'fromEliza');
    return;
  }
  const line = appendLine(log, '', 'fromEliza');
  for (const character of text) {
    line.textContent += character;
    log.scrollTop = log.scrollHeight;
    await wait(teletypeDelayMs);
  }
}
