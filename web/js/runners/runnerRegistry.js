import { basicRunner } from './basicRunner.js';
import { javascriptRunner } from './javascriptRunner.js';
import { recordedOutputRunner } from './recordedOutputRunner.js';

const runnersByRuntime = {
  basic: basicRunner,
  javascript: javascriptRunner,
  recorded: recordedOutputRunner,
};

export function getRunner(runtime) {
  return runnersByRuntime[runtime] ?? recordedOutputRunner;
}
