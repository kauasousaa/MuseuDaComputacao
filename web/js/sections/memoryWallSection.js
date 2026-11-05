import { apiClient } from '../core/apiClient.js';
import { createElement, renderLoadError, showFeedback } from '../core/dom.js';
import { loadEras } from '../core/eraCatalog.js';
import { renderFilterChips } from '../core/filterChips.js';

const maxTextLength = 280;
const minTextLength = 3;
const refreshIntervalMs = 30000;
const allEras = '';
const postItColors = ['#fff59d', '#ffcc80', '#a5d6a7', '#90caf9', '#f48fb1', '#ce93d8', '#80deea'];
const defaultPlaceholder = 'Ex.: Meu primeiro código foi um 10 PRINT no computador da escola...';

export async function initMemoryWallSection() {
  const elements = {
    form: document.getElementById('memoryForm'),
    text: document.getElementById('memoryText'),
    era: document.getElementById('memoryEra'),
    author: document.getElementById('memoryAuthor'),
    consent: document.getElementById('memoryConsent'),
    submit: document.getElementById('memorySubmit'),
    counter: document.getElementById('memoryCounter'),
    feedback: document.getElementById('memoryFeedback'),
    questionList: document.getElementById('guideQuestionList'),
    wall: document.getElementById('memoryWall'),
    wallFilters: document.getElementById('wallFilters'),
    wallCount: document.getElementById('wallCount'),
  };
  const state = { selectedQuestion: null, eraFilter: allEras, renderedSignature: '' };

  setupCounter(elements);
  setupForm(elements, state);
  await Promise.all([loadGuideQuestions(elements, state), loadEraOptions(elements, state)]);
  await refreshWall(elements, state);

  setInterval(() => {
    if (document.visibilityState === 'visible') {
      refreshWall(elements, state);
    }
  }, refreshIntervalMs);
}

function setupCounter({ text, counter }) {
  const update = () => {
    counter.textContent = `${text.value.length}/${maxTextLength}`;
    counter.classList.toggle('isNearLimit', text.value.length > maxTextLength * 0.9);
  };
  text.addEventListener('input', update);
  update();
}

async function loadGuideQuestions(elements, state) {
  try {
    const questions = await apiClient.get('/api/guide-questions');
    const buttons = questions.map((question) => {
      const button = createElement('button', {
        className: 'guideQuestion',
        text: question.text,
        attributes: { type: 'button', 'aria-pressed': 'false' },
      });
      button.addEventListener('click', () => {
        selectGuideQuestion(elements, state, state.selectedQuestion === question.text ? null : question.text);
        elements.text.focus();
      });
      return button;
    });
    elements.questionList.replaceChildren(...buttons);
  } catch (error) {
    renderLoadError(elements.questionList, error.message);
  }
}

function selectGuideQuestion(elements, state, questionText) {
  state.selectedQuestion = questionText;
  elements.questionList.querySelectorAll('.guideQuestion').forEach((button) => {
    const isActive = button.textContent === questionText;
    button.classList.toggle('active', isActive);
    button.setAttribute('aria-pressed', String(isActive));
  });
  elements.text.placeholder = questionText ?? defaultPlaceholder;
}

async function loadEraOptions(elements, state) {
  try {
    const eras = await loadEras();
    elements.era.append(...eras.map((era) => createElement('option', { text: era.label, attributes: { value: era.code } })));
    const filterOptions = [{ value: allEras, label: 'Todas as épocas' }, ...eras.map((era) => ({ value: era.code, label: era.label }))];
    renderFilterChips(elements.wallFilters, filterOptions, (eraCode) => {
      state.eraFilter = eraCode;
      refreshWall(elements, state);
    });
  } catch (error) {
    showFeedback(elements.feedback, error.message, 'error');
  }
}

function setupForm(elements, state) {
  elements.form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const validationMessage = validateForm(elements);
    if (validationMessage) {
      showFeedback(elements.feedback, validationMessage, 'error');
      return;
    }

    elements.submit.disabled = true;
    showFeedback(elements.feedback, 'Colando no mural...');
    try {
      const memory = await apiClient.post('/api/memories', {
        text: elements.text.value,
        era: elements.era.value,
        authorName: elements.author.value,
        guideQuestion: state.selectedQuestion ?? '',
      });
      elements.form.reset();
      elements.text.dispatchEvent(new Event('input'));
      selectGuideQuestion(elements, state, null);
      showFeedback(elements.feedback, 'Obrigado! Sua memória já está no mural.', 'success');
      await refreshWall(elements, state, memory.id);
    } catch (error) {
      showFeedback(elements.feedback, error.message, 'error');
    } finally {
      elements.submit.disabled = false;
    }
  });
}

function validateForm({ text, era, consent }) {
  if (text.value.trim().length < minTextLength) {
    text.focus();
    return 'Conte um pouquinho mais da sua memória.';
  }
  if (!era.value) {
    era.focus();
    return 'Escolha a época aproximada da memória.';
  }
  if (!consent.checked) {
    consent.focus();
    return 'Marque a autorização para exibir a memória no mural.';
  }
  return null;
}

async function refreshWall(elements, state, highlightedId = null) {
  try {
    const query = state.eraFilter ? `?era=${encodeURIComponent(state.eraFilter)}` : '';
    const memories = await apiClient.get(`/api/memories${query}`);
    const signature = `${state.eraFilter}|${memories.map((memory) => memory.id).join(',')}`;
    if (signature === state.renderedSignature && !highlightedId) {
      return;
    }
    state.renderedSignature = signature;
    renderWall(elements, memories, highlightedId);
  } catch (error) {
    renderLoadError(elements.wall, error.message);
  }
}

function renderWall({ wall, wallCount }, memories, highlightedId) {
  wallCount.textContent = `${memories.length} memória(s) no mural`;
  if (memories.length === 0) {
    wall.replaceChildren(createElement('li', {
      className: 'wallEmpty',
      text: 'O mural ainda está vazio. Que tal ser a primeira memória?',
    }));
    return;
  }
  wall.replaceChildren(...memories.map((memory) => createPostIt(memory, memory.id === highlightedId)));
}

function createPostIt(memory, isNew) {
  const hash = hashText(memory.id);
  const postIt = createElement('li', {
    className: isNew ? 'postIt isNew' : 'postIt',
    attributes: { title: new Date(memory.createdAt).toLocaleString('pt-BR') },
  }, [
    memory.guideQuestion ? createElement('span', { className: 'postItQuestion', text: memory.guideQuestion }) : null,
    createElement('p', { className: 'postItText', text: memory.text }),
    createElement('div', { className: 'postItFooter' }, [
      createElement('span', { text: `— ${memory.authorName ?? 'Anônimo'}` }),
      createElement('span', { text: memory.era.label }),
    ]),
  ]);
  postIt.style.setProperty('--postItColor', postItColors[hash % postItColors.length]);
  postIt.style.setProperty('--tilt', `${(hash % 7) - 3}deg`);
  return postIt;
}

function hashText(text) {
  return [...text].reduce((hash, character) => (hash * 31 + character.charCodeAt(0)) >>> 0, 7);
}
