const executionTimeoutMs = 3000;

const workerSource = `
const maxLines = 500;
self.onmessage = (event) => {
  const lines = [];
  const format = (value) => {
    if (typeof value === 'string') return value;
    try { return JSON.stringify(value) ?? String(value); } catch { return String(value); }
  };
  const capture = (...values) => {
    if (lines.length < maxLines) lines.push(values.map(format).join(' '));
  };
  self.console = { log: capture, info: capture, warn: capture, error: capture };
  try {
    new Function(event.data)();
    self.postMessage({ output: lines.join('\\n') });
  } catch (error) {
    self.postMessage({ output: lines.join('\\n'), error: error.name + ': ' + error.message });
  }
};
`;

export const javascriptRunner = {
  isLive: true,
  badgeText: 'Executa de verdade no seu navegador (Web Worker isolado)',

  run(sourceCode) {
    return new Promise((resolve) => {
      const workerUrl = URL.createObjectURL(new Blob([workerSource], { type: 'text/javascript' }));
      const worker = new Worker(workerUrl);
      const startedAt = performance.now();

      const finish = (result) => {
        clearTimeout(timeoutId);
        worker.terminate();
        URL.revokeObjectURL(workerUrl);
        resolve(result);
      };

      const timeoutId = setTimeout(() => finish({
        output: '',
        error: `Tempo limite de ${executionTimeoutMs / 1000}s excedido (loop infinito?)`,
      }), executionTimeoutMs);

      worker.onmessage = (event) => finish({
        ...event.data,
        note: `Executado em ${(performance.now() - startedAt).toFixed(1)} ms pelo motor JavaScript do navegador`,
      });
      worker.onerror = (event) => {
        event.preventDefault();
        finish({ output: '', error: event.message });
      };
      worker.postMessage(sourceCode);
    });
  },
};
