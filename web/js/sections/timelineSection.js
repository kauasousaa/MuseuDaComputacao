import { apiClient } from '../core/apiClient.js';
import { createElement, renderLoadError } from '../core/dom.js';
import { renderFilterChips } from '../core/filterChips.js';
import { observeReveal } from '../core/revealOnScroll.js';

export const categoryLabels = {
  hardware: 'Hardware',
  software: 'Software',
  linguagem: 'Linguagens',
  internet: 'Internet',
  ia: 'IA',
};

const allCategories = 'todos';

export async function initTimelineSection() {
  const list = document.getElementById('timelineList');
  const filters = document.getElementById('timelineFilters');

  let events;
  try {
    events = await apiClient.get('/api/timeline');
  } catch (error) {
    renderLoadError(list, error.message);
    return;
  }

  const filterOptions = [
    { value: allCategories, label: 'Todos' },
    ...Object.entries(categoryLabels).map(([value, label]) => ({ value, label })),
  ];
  renderFilterChips(filters, filterOptions, (category) => renderEvents(list, filterByCategory(events, category)));
  renderEvents(list, events);
}

function filterByCategory(events, category) {
  return category === allCategories ? events : events.filter((event) => event.category === category);
}

function renderEvents(list, events) {
  const items = events.map(createTimelineItem);
  list.replaceChildren(...items);
  observeReveal(items);
}

function createTimelineItem(event) {
  return createElement('li', { className: 'timelineItem', dataset: { category: event.category } }, [
    createElement('article', { className: 'timelineCard' }, [
      createElement('div', { className: 'timelineMeta' }, [
        createElement('span', { className: 'timelineYear', text: event.year }),
        createElement('span', { className: 'categoryBadge', text: categoryLabels[event.category] ?? event.category }),
      ]),
      createElement('h3', { text: event.title }),
      createElement('p', { text: event.description }),
    ]),
  ]);
}
