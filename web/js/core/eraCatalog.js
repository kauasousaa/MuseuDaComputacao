import { apiClient } from './apiClient.js';

let erasRequest = null;

export function loadEras() {
  if (!erasRequest) {
    erasRequest = apiClient.get('/api/eras').catch((error) => {
      erasRequest = null;
      throw error;
    });
  }
  return erasRequest;
}
