// Filion - Website Interactive & GitHub Stats Scripts

document.addEventListener('DOMContentLoaded', () => {
  initNavbarScroll();
  initMobileMenu();
  fetchGitHubStats();
});

// Dynamic Navbar surface styling on scroll
function initNavbarScroll() {
  const header = document.querySelector('.site-header');
  if (!header) return;

  window.addEventListener('scroll', () => {
    if (window.scrollY > 20) {
      header.classList.add('scrolled');
    } else {
      header.classList.remove('scrolled');
    }
  }, { passive: true });
}

// Fullscreen Mobile Navigation Menu with scroll-lock
function initMobileMenu() {
  const toggleBtn = document.getElementById('mobileToggle');
  const overlay = document.getElementById('mobileNavOverlay');
  const iconMenu = document.getElementById('mobileIconMenu');
  const iconClose = document.getElementById('mobileIconClose');
  if (!toggleBtn || !overlay) return;

  let isOpen = false;

  function openMenu() {
    isOpen = true;
    overlay.classList.add('open');
    toggleBtn.setAttribute('aria-expanded', 'true');
    document.body.style.overflow = 'hidden';
    if (iconMenu && iconClose) {
      iconMenu.style.display = 'none';
      iconClose.style.display = 'block';
    }
  }

  function closeMenu() {
    isOpen = false;
    overlay.classList.remove('open');
    toggleBtn.setAttribute('aria-expanded', 'false');
    document.body.style.overflow = '';
    if (iconMenu && iconClose) {
      iconMenu.style.display = 'block';
      iconClose.style.display = 'none';
    }
  }

  toggleBtn.addEventListener('click', () => {
    if (isOpen) {
      closeMenu();
    } else {
      openMenu();
    }
  });

  // Close when clicking any navigation link
  overlay.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => {
      closeMenu();
    });
  });

  // Close on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && isOpen) {
      closeMenu();
    }
  });
}

const numberFormatter = new Intl.NumberFormat();
const reduceMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');

function animateCounter(element, target) {
  if (!element || !Number.isFinite(target)) return;

  const endValue = Math.max(0, Math.trunc(target));
  if (reduceMotionQuery.matches || endValue === 0) {
    element.innerText = numberFormatter.format(endValue);
    return;
  }

  const duration = 800;
  const startTime = performance.now();

  function updateCounter(currentTime) {
    const progress = Math.min((currentTime - startTime) / duration, 1);
    const easedProgress = 1 - Math.pow(1 - progress, 3);
    element.innerText = numberFormatter.format(Math.round(endValue * easedProgress));

    if (progress < 1) {
      requestAnimationFrame(updateCounter);
    }
  }

  requestAnimationFrame(updateCounter);
}

function sumReleaseDownloads(releases) {
  if (!Array.isArray(releases)) return 0;
  return releases.reduce((releaseTotal, release) => {
    const assetTotal = Array.isArray(release.assets)
      ? release.assets.reduce((total, asset) => total + (asset.download_count || 0), 0)
      : 0;
    return releaseTotal + assetTotal;
  }, 0);
}

async function fetchTotalReleaseDownloads() {
  let nextUrl = 'https://api.github.com/repos/qtremors/filion/releases?per_page=100';
  let totalDownloads = 0;

  while (nextUrl) {
    const response = await fetch(nextUrl);
    if (!response.ok) throw new Error(`GitHub releases request failed: ${response.status}`);

    const releases = await response.json();
    totalDownloads += sumReleaseDownloads(releases);

    const nextLink = response.headers.get('link')
      ?.split(',')
      .find(link => link.includes('rel="next"'));
    nextUrl = nextLink?.match(/<([^>]+)>/)?.[1] || '';
  }

  return totalDownloads;
}

// Fetch live GitHub repository statistics and release download counters
async function fetchGitHubStats() {
  const [repoResult, releaseResult, totalDownloadsResult] = await Promise.allSettled([
    fetch('https://api.github.com/repos/qtremors/filion'),
    fetch('https://api.github.com/repos/qtremors/filion/releases/latest'),
    fetchTotalReleaseDownloads()
  ]);

  try {
    const repoRes = repoResult.status === 'fulfilled' ? repoResult.value : null;
    if (repoRes?.ok) {
      const data = await repoRes.json();
      if (data.stargazers_count !== undefined) {
        animateCounter(document.getElementById('gh-stars'), data.stargazers_count);
      }
      if (data.forks_count !== undefined) {
        animateCounter(document.getElementById('gh-forks'), data.forks_count);
      }
    }
  } catch (error) {
    console.warn('Error fetching repository stats:', error);
  }

  try {
    const releaseRes = releaseResult.status === 'fulfilled' ? releaseResult.value : null;
    if (releaseRes?.ok) {
      const release = await releaseRes.json();
      if (release.tag_name) {
        document.querySelectorAll('.download-btn-text').forEach(el => {
          el.innerText = `Download APK (${release.tag_name})`;
        });
      }

      const latestDownloads = sumReleaseDownloads([release]);
      animateCounter(document.getElementById('gh-latest-downloads'), latestDownloads);
    }
  } catch (error) {
    console.warn('Error fetching latest release stats:', error);
  }

  if (totalDownloadsResult.status === 'fulfilled') {
    const totalDownloads = totalDownloadsResult.value;
    animateCounter(document.getElementById('gh-total-downloads'), totalDownloads);
  }
}
