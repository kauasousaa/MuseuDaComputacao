import { apiClient } from '../core/apiClient.js';
import { createElement, renderLoadError } from '../core/dom.js';
import { observeReveal } from '../core/revealOnScroll.js';

export async function initRecognizeSection() {
  const grid = document.getElementById('recognizeGrid');
  const scoreElement = document.getElementById('recognizeScore');

  let challenges;
  try {
    challenges = await apiClient.get('/api/challenges');
  } catch (error) {
    renderLoadError(grid, error.message);
    return;
  }

  const answers = new Map();
  const updateScore = () => renderScore(scoreElement, answers, challenges.length);
  const cards = challenges.map((challenge) => createFlipCard(challenge, (recognized) => {
    answers.set(challenge.id, recognized);
    updateScore();
  }));

  grid.replaceChildren(...cards);
  observeReveal(cards);
  updateScore();
}

function renderScore(scoreElement, answers, total) {
  const recognizedCount = [...answers.values()].filter(Boolean).length;
  scoreElement.replaceChildren(
    'Você reconheceu ',
    createElement('strong', { text: recognizedCount }),
    ` de ${total} · ${answers.size} cartão(ões) respondido(s)`,
  );
}

function createFlipCard(challenge, onAnswer) {
  const card = createElement('div', { className: 'flipCard' });
  const front = createElement('button', {
    className: 'flipFace flipFront',
    attributes: { type: 'button', 'aria-label': 'Revelar a resposta deste cartão' },
  }, [
    createElement('pre', { text: challenge.snippet }),
    createElement('span', { className: 'flipHint', text: 'Você reconhece? Clique para virar ↻' }),
  ]);

  const answerArea = createElement('div', { className: 'flipActions' });
  const knewButton = createElement('button', { className: 'button buttonPrimary', text: 'Eu lembrava!', attributes: { type: 'button' } });
  const forgotButton = createElement('button', { className: 'button buttonGhost', text: 'Não fazia ideia', attributes: { type: 'button' } });
  answerArea.append(knewButton, forgotButton);

  const registerAnswer = (recognized) => {
    onAnswer(recognized);
    answerArea.replaceWith(createElement('p', {
      className: 'flipAnswered',
      text: recognized ? 'Memória de veterano confirmada!' : 'Agora você sabe. Mais uma para a coleção!',
    }));
  };
  knewButton.addEventListener('click', () => registerAnswer(true));
  forgotButton.addEventListener('click', () => registerAnswer(false));

  const back = createElement('div', { className: 'flipFace flipBack' }, [
    createElement('h3', { text: challenge.answer }),
    createElement('p', { className: 'flipPeriod', text: challenge.period }),
    createElement('p', { className: 'flipExplanation', text: challenge.explanation }),
    answerArea,
  ]);
  back.inert = true;

  front.addEventListener('click', () => {
    card.classList.add('isFlipped');
    back.inert = false;
    front.inert = true;
    knewButton.focus({ preventScroll: true });
  });

  card.append(createElement('div', { className: 'flipInner' }, [front, back]));
  return card;
}
