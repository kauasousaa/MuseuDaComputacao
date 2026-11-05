export const recordedOutputRunner = {
  isLive: false,
  badgeText: 'Saída registrada do programa original',

  async run(sourceCode, language) {
    return {
      output: language.sampleOutput,
      note: 'Saída registrada: este compilador não roda no navegador, mas este é o resultado do programa.',
    };
  },
};
