import { apiClient } from '../core/apiClient.js';
import { createElement, renderLoadError, showFeedback } from '../core/dom.js';
import { loadEras } from '../core/eraCatalog.js';

export async function initEraVoteSection() {
  const buttonsContainer = document.getElementById('eraVoteButtons');
  const chart = document.getElementById('eraVoteChart');
  const feedback = document.getElementById('eraVoteFeedback');

  try {
    const [eras, votes] = await Promise.all([loadEras(), apiClient.get('/api/era-votes')]);
    renderChart(chart, votes);
    renderButtons(buttonsContainer, eras, async (eraCode) => {
      try {
        const updatedVotes = await apiClient.post('/api/era-votes', { era: eraCode });
        renderChart(chart, updatedVotes, eraCode);
        showFeedback(feedback, 'Voto registrado! Bem-vindo à turma.', 'success');
      } catch (error) {
        showFeedback(feedback, error.message, 'error');
      }
    });
  } catch (error) {
    renderLoadError(chart, error.message);
  }
}

function renderButtons(container, eras, onVote) {
  const buttons = eras.map((era) => {
    const button = createElement('button', { className: 'chip', text: era.label, attributes: { type: 'button' } });
    button.addEventListener('click', async () => {
      buttons.forEach((other) => {
        other.disabled = true;
      });
      await onVote(era.code);
      buttons.forEach((other) => {
        other.disabled = false;
        other.classList.toggle('active', other === button);
      });
    });
    return button;
  });
  container.replaceChildren(...buttons);
}

function renderChart(chart, votes, highlightedEraCode) {
  const highestVote = Math.max(1, ...votes.map((entry) => entry.votes));
  const columns = votes.map((entry) => {
    const fill = createElement('div', { className: 'barFill' });
    const column = createElement('div', {
      className: entry.era.code === highlightedEraCode ? 'barColumn isMine' : 'barColumn',
      attributes: { title: `${entry.era.label}: ${entry.votes} voto(s)` },
    }, [
      createElement('span', { className: 'barValue', text: entry.votes }),
      fill,
      createElement('span', { className: 'barLabel', text: entry.era.label }),
    ]);
    requestAnimationFrame(() => {
      fill.style.height = `${Math.round((entry.votes / highestVote) * 100)}%`;
    });
    return column;
  });
  chart.replaceChildren(...columns);
}
