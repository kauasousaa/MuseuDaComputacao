import { Perceptron } from './perceptron.js';

const maxEpochs = 100;
const epochDelayMs = 120;
const regionCellSize = 10;
const pointRadius = 7;
const samplePointsPerClass = 14;
const classColors = {
  1: { point: '#00d4ff', region: 'rgba(0, 212, 255, 0.10)' },
  [-1]: { point: '#ffb000', region: 'rgba(255, 176, 0, 0.10)' },
};

export function initPerceptronLab() {
  const canvas = document.getElementById('perceptronCanvas');
  const status = document.getElementById('perceptronStatus');
  const trainButton = document.getElementById('perceptronTrain');
  const segments = [...document.querySelectorAll('[data-point-class]')];
  const context = canvas.getContext('2d');

  const perceptron = new Perceptron();
  const state = { points: [], selectedLabel: 1, isTrained: false, trainingTimer: null };

  const redraw = () => drawScene(context, canvas, state, perceptron);
  const setStatus = (text) => {
    status.textContent = text;
  };

  const stopTraining = () => {
    clearTimeout(state.trainingTimer);
    state.trainingTimer = null;
    trainButton.disabled = false;
  };

  const addPoint = (event, label) => {
    const { x, y } = toNormalizedCoordinates(canvas, event);
    state.points.push({ x, y, label });
    state.isTrained = false;
    redraw();
    setStatus(`${state.points.length} ponto(s). Clique em "Treinar" para o neurônio aprender.`);
  };

  canvas.addEventListener('click', (event) => addPoint(event, event.shiftKey ? -state.selectedLabel : state.selectedLabel));
  canvas.addEventListener('contextmenu', (event) => {
    event.preventDefault();
    addPoint(event, -1);
  });

  segments.forEach((segment) => {
    segment.addEventListener('click', () => {
      state.selectedLabel = Number(segment.dataset.pointClass);
      segments.forEach((other) => {
        other.classList.toggle('active', other === segment);
        other.setAttribute('aria-checked', String(other === segment));
      });
    });
  });

  document.getElementById('perceptronSample').addEventListener('click', () => {
    stopTraining();
    state.points = createSamplePoints();
    state.isTrained = false;
    perceptron.reset();
    redraw();
    setStatus('Exemplo carregado: duas nuvens de pontos. Agora clique em "Treinar".');
  });

  document.getElementById('perceptronClear').addEventListener('click', () => {
    stopTraining();
    state.points = [];
    state.isTrained = false;
    perceptron.reset();
    redraw();
    setStatus('Área limpa. Clique para adicionar pontos (botão direito adiciona Classe B).');
  });

  trainButton.addEventListener('click', () => {
    const hasBothClasses = new Set(state.points.map((point) => point.label)).size === 2;
    if (!hasBothClasses) {
      setStatus('Adicione pontos das duas classes antes de treinar.');
      return;
    }
    stopTraining();
    trainButton.disabled = true;
    perceptron.reset();
    state.isTrained = true;
    runEpoch(1);
  });

  const runEpoch = (epoch) => {
    const errorCount = perceptron.trainEpoch(state.points);
    redraw();
    const [weightX, weightY] = perceptron.weights.map((weight) => weight.toFixed(2));
    const summary = `Época ${epoch} · erros: ${errorCount} · w1=${weightX} w2=${weightY} b=${perceptron.bias.toFixed(2)}`;

    if (errorCount === 0) {
      setStatus(`${summary} · Convergiu! O neurônio separou as classes.`);
      stopTraining();
    } else if (epoch >= maxEpochs) {
      setStatus(`${summary} · Não convergiu: um único perceptron só separa classes com uma reta (a crítica de Minsky e Papert, 1969).`);
      stopTraining();
    } else {
      setStatus(summary);
      state.trainingTimer = setTimeout(() => runEpoch(epoch + 1), epochDelayMs);
    }
  };

  redraw();
}

function toNormalizedCoordinates(canvas, event) {
  const bounds = canvas.getBoundingClientRect();
  const pixelX = ((event.clientX - bounds.left) / bounds.width) * canvas.width;
  const pixelY = ((event.clientY - bounds.top) / bounds.height) * canvas.height;
  return {
    x: (pixelX / canvas.width) * 2 - 1,
    y: 1 - (pixelY / canvas.height) * 2,
  };
}

function toPixel(canvas, x, y) {
  return {
    pixelX: ((x + 1) / 2) * canvas.width,
    pixelY: ((1 - y) / 2) * canvas.height,
  };
}

function createSamplePoints() {
  const clusters = [
    { centerX: -0.45, centerY: 0.35, label: 1 },
    { centerX: 0.45, centerY: -0.35, label: -1 },
  ];
  return clusters.flatMap(({ centerX, centerY, label }) => Array.from({ length: samplePointsPerClass }, () => ({
    x: centerX + (Math.random() - 0.5) * 0.7,
    y: centerY + (Math.random() - 0.5) * 0.7,
    label,
  })));
}

function drawScene(context, canvas, state, perceptron) {
  context.clearRect(0, 0, canvas.width, canvas.height);
  drawGrid(context, canvas);
  if (state.isTrained) {
    drawRegions(context, canvas, perceptron);
    drawDecisionLine(context, canvas, perceptron);
  }
  state.points.forEach((point) => drawPoint(context, canvas, point));
}

function drawGrid(context, canvas) {
  context.strokeStyle = 'rgba(255, 255, 255, 0.05)';
  context.lineWidth = 1;
  for (let x = 0; x <= canvas.width; x += 40) {
    context.beginPath();
    context.moveTo(x, 0);
    context.lineTo(x, canvas.height);
    context.stroke();
  }
  for (let y = 0; y <= canvas.height; y += 40) {
    context.beginPath();
    context.moveTo(0, y);
    context.lineTo(canvas.width, y);
    context.stroke();
  }
}

function drawRegions(context, canvas, perceptron) {
  for (let pixelX = 0; pixelX < canvas.width; pixelX += regionCellSize) {
    for (let pixelY = 0; pixelY < canvas.height; pixelY += regionCellSize) {
      const x = ((pixelX + regionCellSize / 2) / canvas.width) * 2 - 1;
      const y = 1 - ((pixelY + regionCellSize / 2) / canvas.height) * 2;
      context.fillStyle = classColors[perceptron.predict(x, y)].region;
      context.fillRect(pixelX, pixelY, regionCellSize, regionCellSize);
    }
  }
}

function drawDecisionLine(context, canvas, perceptron) {
  const [weightX, weightY] = perceptron.weights;
  const { bias } = perceptron;
  let start;
  let end;
  if (Math.abs(weightY) > 1e-6) {
    start = toPixel(canvas, -1, -(weightX * -1 + bias) / weightY);
    end = toPixel(canvas, 1, -(weightX * 1 + bias) / weightY);
  } else if (Math.abs(weightX) > 1e-6) {
    start = toPixel(canvas, -bias / weightX, -1);
    end = toPixel(canvas, -bias / weightX, 1);
  } else {
    return;
  }
  context.strokeStyle = '#ffffff';
  context.lineWidth = 2;
  context.setLineDash([8, 6]);
  context.beginPath();
  context.moveTo(start.pixelX, start.pixelY);
  context.lineTo(end.pixelX, end.pixelY);
  context.stroke();
  context.setLineDash([]);
}

function drawPoint(context, canvas, point) {
  const { pixelX, pixelY } = toPixel(canvas, point.x, point.y);
  context.beginPath();
  context.arc(pixelX, pixelY, pointRadius, 0, Math.PI * 2);
  context.fillStyle = classColors[point.label].point;
  context.shadowColor = classColors[point.label].point;
  context.shadowBlur = 12;
  context.fill();
  context.shadowBlur = 0;
  context.lineWidth = 2;
  context.strokeStyle = '#0a0d14';
  context.stroke();
}
