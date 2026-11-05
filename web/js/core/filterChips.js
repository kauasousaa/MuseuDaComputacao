import { createElement } from './dom.js';

export function renderFilterChips(container, options, onChange) {
  const chips = options.map((option, index) => createElement('button', {
    className: index === 0 ? 'chip active' : 'chip',
    text: option.label,
    attributes: { type: 'button', 'aria-pressed': String(index === 0) },
    dataset: { category: option.value },
  }));

  chips.forEach((chip, index) => {
    chip.addEventListener('click', () => {
      chips.forEach((other) => {
        other.classList.toggle('active', other === chip);
        other.setAttribute('aria-pressed', String(other === chip));
      });
      onChange(options[index].value);
    });
  });

  container.replaceChildren(...chips);
}
