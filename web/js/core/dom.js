export function createElement(tagName, options = {}, children = []) {
  const { className, text, attributes = {}, dataset = {} } = options;
  const element = document.createElement(tagName);
  if (className) {
    element.className = className;
  }
  if (text !== undefined && text !== null) {
    element.textContent = text;
  }
  Object.entries(attributes).forEach(([name, value]) => element.setAttribute(name, value));
  Object.entries(dataset).forEach(([key, value]) => {
    element.dataset[key] = value;
  });
  children.filter(Boolean).forEach((child) => element.append(child));
  return element;
}

export function renderLoadError(container, message) {
  const isList = ['UL', 'OL'].includes(container.tagName);
  container.replaceChildren(createElement(isList ? 'li' : 'p', { className: 'loadError', text: message }));
}

export function showFeedback(element, message, type = 'info') {
  element.textContent = message;
  element.classList.toggle('isError', type === 'error');
  element.classList.toggle('isSuccess', type === 'success');
}

export function wait(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

export function prefersReducedMotion() {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}
