import { apiClient } from '../core/apiClient.js';
import { createElement, prefersReducedMotion, renderLoadError } from '../core/dom.js';
import { observeReveal } from '../core/revealOnScroll.js';

export async function initComparisonSection() {
  initBeforeAfterSlider();

  const grid = document.getElementById('comparisonGrid');
  try {
    const comparisons = await apiClient.get('/api/comparisons');
    const cards = comparisons.map(createComparisonCard);
    grid.replaceChildren(...cards);
    observeReveal(cards);
  } catch (error) {
    renderLoadError(grid, error.message);
  }
}

function createComparisonCard(comparison) {
  return createElement('article', { className: 'comparisonCard' }, [
    createElement('h3', { text: comparison.theme }),
    createElement('div', { className: 'comparisonHalves' }, [
      createElement('div', { className: 'comparisonPast' }, [
        createElement('span', { className: 'halfLabel', text: `Antes · ${comparison.pastPeriod}` }),
        createElement('p', { text: comparison.past }),
      ]),
      createElement('div', { className: 'comparisonPresent' }, [
        createElement('span', { className: 'halfLabel', text: 'Agora' }),
        createElement('p', { text: comparison.present }),
      ]),
    ]),
    createElement('p', { className: 'comparisonCuriosity' }, [
      createElement('strong', { text: 'Curiosidade: ' }),
      comparison.curiosity,
    ]),
  ]);
}

function initBeforeAfterSlider() {
  const stage = document.getElementById('beforeAfterStage');
  const range = document.getElementById('beforeAfterRange');

  const setSplit = (percentage) => {
    const clamped = Math.min(100, Math.max(0, percentage));
    stage.style.setProperty('--split', `${clamped}%`);
    range.value = String(Math.round(clamped));
  };

  const splitFromPointer = (event) => {
    const bounds = stage.getBoundingClientRect();
    setSplit(((event.clientX - bounds.left) / bounds.width) * 100);
  };

  let cancelDemo = playSweepDemoWhenVisible(stage, setSplit);
  const stopDemo = () => {
    cancelDemo();
    cancelDemo = () => {};
  };

  range.addEventListener('input', () => {
    stopDemo();
    setSplit(Number(range.value));
  });
  stage.addEventListener('pointerdown', (event) => {
    stopDemo();
    stage.setPointerCapture(event.pointerId);
    splitFromPointer(event);
  });
  stage.addEventListener('pointermove', (event) => {
    if (stage.hasPointerCapture(event.pointerId)) {
      splitFromPointer(event);
    }
  });
}

function playSweepDemoWhenVisible(stage, setSplit) {
  if (prefersReducedMotion() || !('IntersectionObserver' in window)) {
    return () => {};
  }
  const keyframes = [50, 92, 8, 50];
  const segmentDurationMs = 1100;
  let animationFrameId = null;

  const observer = new IntersectionObserver((entries) => {
    if (!entries.some((entry) => entry.isIntersecting)) {
      return;
    }
    observer.disconnect();
    const startedAt = performance.now();
    const animate = (now) => {
      const progress = (now - startedAt) / segmentDurationMs;
      const segment = Math.floor(progress);
      if (segment >= keyframes.length - 1) {
        setSplit(keyframes[keyframes.length - 1]);
        return;
      }
      const easing = 0.5 - Math.cos((progress - segment) * Math.PI) / 2;
      setSplit(keyframes[segment] + (keyframes[segment + 1] - keyframes[segment]) * easing);
      animationFrameId = requestAnimationFrame(animate);
    };
    animationFrameId = requestAnimationFrame(animate);
  }, { threshold: 0.6 });
  observer.observe(stage);

  return () => {
    observer.disconnect();
    cancelAnimationFrame(animationFrameId);
  };
}
