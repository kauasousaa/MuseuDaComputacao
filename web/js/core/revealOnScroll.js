const visibilityThreshold = 0.12;

const observer = 'IntersectionObserver' in window
  ? new IntersectionObserver((entries) => {
    entries
      .filter((entry) => entry.isIntersecting)
      .forEach((entry) => {
        entry.target.classList.add('isVisible');
        observer.unobserve(entry.target);
      });
  }, { threshold: visibilityThreshold })
  : null;

export function observeReveal(elements) {
  elements.forEach((element) => {
    element.classList.add('reveal');
    if (observer) {
      observer.observe(element);
    } else {
      element.classList.add('isVisible');
    }
  });
}
