import { apiClient } from '../core/apiClient.js';

export const basicRunner = {
  isLive: true,
  badgeText: 'Executa de verdade: interpretador BASIC em Java no servidor',

  async run(sourceCode) {
    const result = await apiClient.post('/api/interpreters/basic', { source: sourceCode });
    return {
      output: result.output,
      error: result.error,
      note: `READY. (${result.steps} linhas executadas pelo interpretador do museu)`,
    };
  },
};
