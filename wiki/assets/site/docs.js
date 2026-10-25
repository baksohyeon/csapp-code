(() => {
  const root = document.documentElement;
  const themeButton = document.querySelector('[data-theme-toggle]');
  const prefersDark = () => matchMedia('(prefers-color-scheme: dark)').matches;
  const isDark = () => root.dataset.theme ? root.dataset.theme === 'dark' : prefersDark();
  const syncThemeButton = () => {
    if (!themeButton) return;
    themeButton.textContent = isDark() ? '밝게' : '어둡게';
    themeButton.setAttribute('aria-label', isDark() ? '밝은 테마로 전환' : '어두운 테마로 전환');
  };
  themeButton?.addEventListener('click', () => {
    root.dataset.theme = isDark() ? 'light' : 'dark';
    try { localStorage.setItem('csapp-theme', root.dataset.theme); } catch {}
    syncThemeButton();
  });
  matchMedia('(prefers-color-scheme: dark)').addEventListener('change', syncThemeButton);
  syncThemeButton();

  const links = [...document.querySelectorAll('.docs-outline a[data-section]')];
  const headings = links.map(a => ({a, element: document.getElementById(a.dataset.section)})).filter(x => x.element);
  let pending = false;
  const update = () => {
    const limit = (document.querySelector('.docs-header')?.getBoundingClientRect().height || 0) + 40;
    let current = headings[0];
    for (const item of headings) if (item.element.getBoundingClientRect().top <= limit) current = item;
    for (const item of headings) {
      if (item === current) item.a.setAttribute('aria-current', 'location');
      else item.a.removeAttribute('aria-current');
    }
    pending = false;
  };
  addEventListener('scroll', () => { if (!pending) { pending = true; requestAnimationFrame(update); } }, {passive:true});
  update();
})();
