const masterVolume = 0.22;
const dialedNumber = '08007012345';
const dtmfFrequencies = {
  1: [697, 1209], 2: [697, 1336], 3: [697, 1477],
  4: [770, 1209], 5: [770, 1336], 6: [770, 1477],
  7: [852, 1209], 8: [852, 1336], 9: [852, 1477],
  0: [941, 1336],
};
const handshakeFrequencies = [980, 1180, 1650, 1850, 2100, 2400];

export class DialupSoundPlayer {
  constructor() {
    this.audioContext = null;
  }

  get isPlaying() {
    return this.audioContext !== null;
  }

  play(onFinished) {
    this.stop();
    const AudioContextClass = window.AudioContext ?? window.webkitAudioContext;
    if (!AudioContextClass) {
      throw new Error('Seu navegador não suporta Web Audio.');
    }
    const audioContext = new AudioContextClass();
    this.audioContext = audioContext;

    const output = audioContext.createGain();
    output.gain.value = masterVolume;
    output.connect(audioContext.destination);

    const totalDuration = scheduleHandshake(audioContext, output, audioContext.currentTime + 0.05);
    setTimeout(() => {
      if (this.audioContext === audioContext) {
        this.stop();
        onFinished?.();
      }
    }, totalDuration * 1000);
  }

  stop() {
    if (this.audioContext) {
      this.audioContext.close();
      this.audioContext = null;
    }
  }
}

function scheduleHandshake(audioContext, output, startTime) {
  let time = startTime;

  scheduleTone(audioContext, output, [350, 440], time, 1.2);
  time += 1.4;

  [...dialedNumber].forEach((digit) => {
    scheduleTone(audioContext, output, dtmfFrequencies[digit], time, 0.09);
    time += 0.16;
  });
  time += 0.5;

  scheduleTone(audioContext, output, [2100], time, 1.4, 0.8);
  scheduleNoise(audioContext, output, time, 1.4, { frequency: 2100, quality: 4, volume: 0.15 });
  time += 1.5;

  scheduleFrequencyShifting(audioContext, output, time, 1.1, 0.03);
  time += 1.2;

  scheduleTone(audioContext, output, [1200, 2400], time, 0.5, 0.5);
  time += 0.5;

  scheduleNoise(audioContext, output, time, 2.4, { frequency: 1800, quality: 0.7, volume: 0.9 });
  scheduleFrequencyShifting(audioContext, output, time, 2.4, 0.012, 0.35);
  time += 2.4;

  return time - startTime + 0.2;
}

function scheduleTone(audioContext, output, frequencies, startTime, duration, volume = 0.5) {
  frequencies.forEach((frequency) => {
    const oscillator = audioContext.createOscillator();
    const gain = createEnvelope(audioContext, startTime, duration, volume / frequencies.length);
    oscillator.frequency.value = frequency;
    oscillator.connect(gain).connect(output);
    oscillator.start(startTime);
    oscillator.stop(startTime + duration + 0.05);
  });
}

function scheduleFrequencyShifting(audioContext, output, startTime, duration, symbolSeconds, volume = 0.5) {
  const oscillator = audioContext.createOscillator();
  oscillator.type = 'square';
  for (let time = startTime; time < startTime + duration; time += symbolSeconds) {
    const frequency = handshakeFrequencies[Math.floor(Math.random() * handshakeFrequencies.length)];
    oscillator.frequency.setValueAtTime(frequency, time);
  }
  const gain = createEnvelope(audioContext, startTime, duration, volume * 0.35);
  oscillator.connect(gain).connect(output);
  oscillator.start(startTime);
  oscillator.stop(startTime + duration + 0.05);
}

function scheduleNoise(audioContext, output, startTime, duration, { frequency, quality, volume }) {
  const sampleCount = Math.ceil(audioContext.sampleRate * duration);
  const buffer = audioContext.createBuffer(1, sampleCount, audioContext.sampleRate);
  const samples = buffer.getChannelData(0);
  for (let index = 0; index < sampleCount; index++) {
    samples[index] = Math.random() * 2 - 1;
  }

  const source = audioContext.createBufferSource();
  source.buffer = buffer;
  const filter = audioContext.createBiquadFilter();
  filter.type = 'bandpass';
  filter.frequency.value = frequency;
  filter.Q.value = quality;
  const gain = createEnvelope(audioContext, startTime, duration, volume);

  source.connect(filter).connect(gain).connect(output);
  source.start(startTime);
  source.stop(startTime + duration + 0.05);
}

function createEnvelope(audioContext, startTime, duration, volume) {
  const gain = audioContext.createGain();
  gain.gain.setValueAtTime(0, startTime);
  gain.gain.linearRampToValueAtTime(volume, startTime + 0.01);
  gain.gain.setValueAtTime(volume, startTime + duration - 0.01);
  gain.gain.linearRampToValueAtTime(0, startTime + duration);
  return gain;
}
