export function initNavigation() {
  const menuToggle = document.getElementById('menuToggle');
  const mainNav = document.getElementById('mainNav');
  const links = [...mainNav.querySelectorAll('a')];

  const setMenuOpen = (isOpen) => {
    mainNav.classList.toggle('isOpen', isOpen);
    menuToggle.setAttribute('aria-expanded', String(isOpen));
  };

  menuToggle.addEventListener('click', () => setMenuOpen(!mainNav.classList.contains('isOpen')));
  links.forEach((link) => link.addEventListener('click', () => setMenuOpen(false)));

  highlightActiveSection(links);
}

function highlightActiveSection(links) {
  if (!('IntersectionObserver' in window)) {
    return;
  }
  const linkBySectionId = new Map(links.map((link) => [link.hash.slice(1), link]));
  const observer = new IntersectionObserver((entries) => {
    entries
      .filter((entry) => entry.isIntersecting)
      .forEach((entry) => {
        links.forEach((link) => link.classList.remove('active'));
        linkBySectionId.get(entry.target.id)?.classList.add('active');
      });
  }, { rootMargin: '-45% 0px -50% 0px' });

  linkBySectionId.forEach((link, sectionId) => {
    const section = document.getElementById(sectionId);
    if (section) {
      observer.observe(section);
    }
  });
}
