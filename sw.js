const CACHE = 'info-delegue-v5';
const ASSETS = ['./', './index.html', './manifest.webmanifest', './icon.svg', './reglement/reglement-general-championnats-nationaux-2026-2027.pdf', './reglement/reglement-championnats-regionaux-seniors-masculins-2026-2027.pdf', './reglement/reglement-coupe-de-france-credit-agricole-2026-2027.pdf', './reglement/reglement-coupe-pays-de-la-loire-seniors-masculins-2026-2027.pdf', './reglement/reglement-coupe-gambardella-credit-agricole-2026-2027.pdf', './reglement/reglement-coupe-nationale-futsal-2026-2027.pdf.pdf.pdf', './reglement/reglement-championnats-regionaux-departementaux-seniors-futsal-lfpl-2026-2027.pdf'];
self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(ASSETS)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key.startsWith('info-delegue-') && key !== CACHE).map(key => caches.delete(key)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;
  event.respondWith(caches.match(event.request).then(cached => {
    if (cached) return cached;
    return fetch(event.request).then(response => {
      if (response && response.ok && new URL(event.request.url).origin === self.location.origin) {
        const copy = response.clone();
        caches.open(CACHE).then(cache => cache.put(event.request, copy));
      }
      return response;
    }).catch(() => caches.match('./index.html'));
  }));
});
