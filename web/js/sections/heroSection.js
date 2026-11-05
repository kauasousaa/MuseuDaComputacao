import { DialupSoundPlayer } from '../audio/dialupSound.js';
import { prefersReducedMotion, wait } from '../core/dom.js';

const bootLines = [
  'MUSEU BIOS v1.0   (C) 1981-2026',
  'CPU: 80486DX2-66MHz',
  'Teste de memória: 640K OK',
  '',
  'Iniciando MS-DOS...',
  '',
  'C:\\> CD \\HISTORIA',
  'C:\\HISTORIA> MUSEU.EXE',
  '',
  'Carregando 1642 ... 2026',
  '[####################] 100%',
  '',
  'Bem-vindo ao Museu da Computação.',
  'Role a página para começar.',
];
const characterDelayMs = 16;
const lineDelayMs = 120;

export function initHeroSection() {
  typeBootLog(document.getElementById('bootLog'));
  setupDialupButton(document.getElementById('dialupButton'));
}

async function typeBootLog(target) {
  if (prefersReducedMotion()) {
    target.textContent = bootLines.join('\n');
    return;
  }
  for (const line of bootLines) {
    for (const character of line) {
      target.textContent += character;
      await wait(characterDelayMs);
    }
    target.textContent += '\n';
    await wait(lineDelayMs);
  }
  target.textContent += 'C:\\HISTORIA> _';
}

function setupDialupButton(button) {
  const player = new DialupSoundPlayer();
  const icon = button.querySelector('.buttonIcon');
  const label = button.querySelector('.buttonLabel');

  const renderState = () => {
    icon.textContent = player.isPlaying ? '■' : '▶';
    label.textContent = player.isPlaying ? 'Desligar o modem' : 'Ouvir a internet discada';
  };

  button.addEventListener('click', () => {
    if (player.isPlaying) {
      player.stop();
    } else {
      try {
        player.play(renderState);
      } catch (error) {
        label.textContent = error.message;
        return;
      }
    }
    renderState();
  });
}
