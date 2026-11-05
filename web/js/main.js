import { initNavigation } from './core/navigation.js';
import { initAiSection } from './sections/aiSection.js';
import { initComparisonSection } from './sections/comparisonSection.js';
import { initEraVoteSection } from './sections/eraVoteSection.js';
import { initHeroSection } from './sections/heroSection.js';
import { initLanguageSection } from './sections/languageSection.js';
import { initMemoryWallSection } from './sections/memoryWallSection.js';
import { initRecognizeSection } from './sections/recognizeSection.js';
import { initTimelineSection } from './sections/timelineSection.js';

const sectionInitializers = [
  initNavigation,
  initHeroSection,
  initTimelineSection,
  initEraVoteSection,
  initComparisonSection,
  initLanguageSection,
  initRecognizeSection,
  initAiSection,
  initMemoryWallSection,
];

sectionInitializers.forEach((initialize) => {
  Promise.resolve()
    .then(initialize)
    .catch((error) => console.error(`Falha ao iniciar ${initialize.name}:`, error));
});
