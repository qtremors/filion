const header = document.querySelector("#site-header");
const menuButton = document.querySelector("#menu-toggle");
const mobileMenu = document.querySelector("#mobile-menu");
const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

function setMenu(open) {
  menuButton.setAttribute("aria-expanded", String(open));
  menuButton.setAttribute("aria-label", open ? "Close navigation" : "Open navigation");
  mobileMenu.hidden = !open;
  header.classList.toggle("menu-open", open);
}

menuButton.addEventListener("click", () => {
  setMenu(menuButton.getAttribute("aria-expanded") !== "true");
});

mobileMenu.querySelectorAll("a").forEach((link) => {
  link.addEventListener("click", () => setMenu(false));
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && !mobileMenu.hidden) {
    setMenu(false);
    menuButton.focus();
  }
});

window.addEventListener("resize", () => {
  if (window.innerWidth > 1040 && !mobileMenu.hidden) setMenu(false);
});

function updateHeader() {
  header.classList.toggle("scrolled", window.scrollY > 16);
}

updateHeader();
window.addEventListener("scroll", updateHeader, { passive: true });

document.querySelectorAll(".faq-item button").forEach((button) => {
  button.addEventListener("click", () => {
    const answer = document.getElementById(button.getAttribute("aria-controls"));
    const willOpen = button.getAttribute("aria-expanded") !== "true";
    button.setAttribute("aria-expanded", String(willOpen));
    answer.hidden = !willOpen;
  });
});

const revealItems = document.querySelectorAll(".reveal");

if (reduceMotion.matches || !("IntersectionObserver" in window)) {
  revealItems.forEach((item) => item.classList.add("visible"));
} else {
  const revealObserver = new IntersectionObserver(
    (entries, observer) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        entry.target.classList.add("visible");
        observer.unobserve(entry.target);
      });
    },
    { rootMargin: "0px 0px -8%", threshold: 0.08 },
  );
  revealItems.forEach((item) => revealObserver.observe(item));
}

const compactNumber = new Intl.NumberFormat("en", { notation: "compact", maximumFractionDigits: 1 });

function displayNumber(value) {
  return compactNumber.format(Math.max(0, value));
}

async function loadGitHubDetails() {
  const repoRequest = fetch("https://api.github.com/repos/qtremors/filion", {
    headers: { Accept: "application/vnd.github+json" },
  });
  const releasesRequest = fetch("https://api.github.com/repos/qtremors/filion/releases?per_page=30", {
    headers: { Accept: "application/vnd.github+json" },
  });

  const [repoResult, releasesResult] = await Promise.allSettled([repoRequest, releasesRequest]);

  if (repoResult.status === "fulfilled" && repoResult.value.ok) {
    const repo = await repoResult.value.json();
    document.querySelector("#github-stars").textContent = displayNumber(repo.stargazers_count);
    document.querySelector("#github-forks").textContent = displayNumber(repo.forks_count);
  }

  if (releasesResult.status === "fulfilled" && releasesResult.value.ok) {
    const releases = await releasesResult.value.json();
    const totalDownloads = releases.reduce(
      (total, release) => total + release.assets.reduce((sum, asset) => sum + asset.download_count, 0),
      0,
    );
    document.querySelector("#github-downloads").textContent = displayNumber(totalDownloads);

    const latest = releases.find((release) => !release.draft && !release.prerelease);
    if (latest) {
      document.querySelectorAll("[data-download-label]").forEach((label) => {
        label.textContent = `Get ${latest.tag_name}`;
      });
    }
  }
}

loadGitHubDetails().catch(() => {
  // Static labels and links remain useful when the API is unavailable or rate limited.
});
