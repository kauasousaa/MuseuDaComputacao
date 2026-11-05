import { apiClient } from '../core/apiClient.js';
import { createElement, prefersReducedMotion, renderLoadError, wait } from '../core/dom.js';
import { getRunner } from '../runners/runnerRegistry.js';

const outputLineDelayMs = 70;
const indentation = '    ';

export async function initLanguageSection() {
  const rail = document.getElementById('languageRail');
  const detail = document.getElementById('languageDetail');

  let languages;
  try {
    languages = await apiClient.get('/api/languages');
  } catch (error) {
    renderLoadError(detail, error.message);
    return;
  }

  const tabs = languages.map((language) => createLanguageTab(language));
  tabs.forEach((tab, index) => {
    tab.addEventListener('click', () => {
      tabs.forEach((other) => {
        other.classList.toggle('active', other === tab);
        other.setAttribute('aria-selected', String(other === tab));
      });
      renderLanguageDetail(detail, languages[index]);
    });
  });
  rail.replaceChildren(...tabs);
  tabs[0]?.click();
}

function createLanguageTab(language) {
  const runner = getRunner(language.runtime);
  return createElement('button', {
    className: 'languageTab',
    attributes: { type: 'button', role: 'tab', 'aria-selected': 'false' },
  }, [
    createElement('span', { className: 'languageTabYear', text: language.year }),
    createElement('span', { className: 'languageTabName', text: language.name }),
    runner.isLive ? createElement('span', { className: 'languageTabLive', text: '● executável' }) : null,
  ]);
}

function renderLanguageDetail(container, language) {
  const runner = getRunner(language.runtime);
  const codeWindow = createCodeWindow(language, runner);
  const info = createElement('div', { className: 'languageInfo' }, [
    createElement('h3', { text: language.name }),
    createElement('dl', { className: 'languageFacts' }, [
      createElement('dt', { text: 'Ano' }), createElement('dd', { text: language.year }),
      createElement('dt', { text: 'Criação' }), createElement('dd', { text: language.creator }),
      createElement('dt', { text: 'Paradigma' }), createElement('dd', { text: language.paradigm }),
    ]),
    createElement('p', { className: 'languageSummary', text: language.summary }),
    createElement('span', { className: runner.isLive ? 'runtimeBadge isLive' : 'runtimeBadge', text: runner.badgeText }),
    language.runtime === 'basic' ? createBasicHelp() : null,
  ]);
  container.replaceChildren(info, codeWindow);
}

function createBasicHelp() {
  const commands = ['PRINT', 'LET', 'IF...THEN', 'FOR...NEXT', 'GOTO', 'GOSUB/RETURN', 'REM', 'END'];
  const functions = ['INT', 'RND', 'ABS', 'SQR', 'LEN', 'STR$'];
  return createElement('div', { className: 'basicHelp' }, [
    createElement('strong', { text: 'Comandos suportados: ' }),
    ...commands.flatMap((command) => [createElement('code', { text: command }), ' ']),
    createElement('br'),
    createElement('strong', { text: 'Funções: ' }),
    ...functions.flatMap((name) => [createElement('code', { text: name }), ' ']),
    createElement('br'),
    'Tente o clássico das lojas de informática: ',
    createElement('code', { text: '10 PRINT "OLA"' }),
    ' e ',
    createElement('code', { text: '20 GOTO 10' }),
  ]);
}

function createCodeWindow(language, runner) {
  const editor = createElement('textarea', {
    className: 'codeEditor',
    attributes: { spellcheck: 'false', 'aria-label': `Código em ${language.name}` },
  });
  editor.value = language.sampleCode;
  editor.readOnly = !runner.isLive;
  editor.rows = Math.max(10, language.sampleCode.split('\n').length + 1);
  if (runner.isLive) {
    enableTabIndentation(editor);
  }

  const output = createElement('pre', { className: 'codeOutput', attributes: { 'aria-live': 'polite' } }, [
    createElement('span', { className: 'outputNote', text: 'Pressione "Executar" para rodar o programa.' }),
  ]);

  const runButton = createElement('button', {
    className: 'button buttonSmall buttonPrimary',
    text: '▶ Executar',
    attributes: { type: 'button' },
  });
  runButton.addEventListener('click', () => executeProgram(runner, language, editor, output, runButton));

  const resetButton = runner.isLive
    ? createElement('button', { className: 'button buttonSmall buttonGhost', text: 'Restaurar', attributes: { type: 'button' } })
    : null;
  resetButton?.addEventListener('click', () => {
    editor.value = language.sampleCode;
    editor.focus();
  });

  return createElement('div', { className: `codeWindow theme-${language.theme}` }, [
    createElement('div', { className: 'codeWindowBar' }, [
      createElement('span', { text: language.fileName }),
      createElement('div', { className: 'codeWindowActions' }, [resetButton, runButton]),
    ]),
    editor,
    output,
  ]);
}

async function executeProgram(runner, language, editor, output, runButton) {
  runButton.disabled = true;
  output.replaceChildren(createElement('span', { className: 'outputNote', text: 'Executando...' }));
  try {
    const result = await runner.run(editor.value, language);
    await renderOutput(output, result);
  } catch (error) {
    output.replaceChildren(createElement('span', { className: 'outputError', text: error.message }));
  } finally {
    runButton.disabled = false;
  }
}

async function renderOutput(output, { output: text, error, note }) {
  output.replaceChildren();
  const lines = text ? text.replace(/\n$/, '').split('\n') : [];
  for (const line of lines) {
    output.append(`${line}\n`);
    output.scrollTop = output.scrollHeight;
    if (!prefersReducedMotion() && lines.length <= 60) {
      await wait(outputLineDelayMs);
    }
  }
  if (error) {
    output.append(createElement('span', { className: 'outputError', text: `${error}\n` }));
  }
  if (note) {
    output.append(createElement('span', { className: 'outputNote', text: note }));
  }
  output.scrollTop = output.scrollHeight;
}

function enableTabIndentation(editor) {
  editor.addEventListener('keydown', (event) => {
    if (event.key !== 'Tab' || event.shiftKey) {
      return;
    }
    event.preventDefault();
    const { selectionStart, selectionEnd, value } = editor;
    editor.value = value.slice(0, selectionStart) + indentation + value.slice(selectionEnd);
    editor.selectionStart = selectionStart + indentation.length;
    editor.selectionEnd = editor.selectionStart;
  });
}
