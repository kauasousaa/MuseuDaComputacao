export class Perceptron {
  constructor(learningRate = 0.1) {
    this.learningRate = learningRate;
    this.reset();
  }

  reset() {
    this.weights = [Math.random() - 0.5, Math.random() - 0.5];
    this.bias = Math.random() - 0.5;
  }

  activation(x, y) {
    return this.weights[0] * x + this.weights[1] * y + this.bias;
  }

  predict(x, y) {
    return this.activation(x, y) >= 0 ? 1 : -1;
  }

  trainEpoch(points) {
    let errorCount = 0;
    points.forEach(({ x, y, label }) => {
      const error = label - this.predict(x, y);
      if (error !== 0) {
        errorCount++;
        this.weights[0] += this.learningRate * error * x;
        this.weights[1] += this.learningRate * error * y;
        this.bias += this.learningRate * error;
      }
    });
    return errorCount;
  }
}
