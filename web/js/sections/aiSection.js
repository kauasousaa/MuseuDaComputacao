import { initElizaChat } from '../ai/elizaChat.js';
import { initPerceptronLab } from '../ai/perceptronLab.js';
import { apiClient } from '../core/apiClient.js';
import { createElement, renderLoadError } from '../core/dom.js';

export async function initAiSection() {
  initElizaChat();
  initPerceptronLab();
  await renderAiTimeline(document.getElementById('aiTimeline'));
}

async function renderAiTimeline(list) {
  try {
    const milestones = await apiClient.get('/api/timeline?category=ia');
    list.replaceChildren(...milestones.map((milestone) => createElement('li', {
      className: 'aiStep',
      attributes: { title: milestone.description },
    }, [
      createElement('span', { className: 'aiStepYear', text: milestone.year }),
      createElement('p', { className: 'aiStepTitle', text: milestone.title }),
    ])));
  } catch (error) {
    renderLoadError(list, error.message);
  }
}
