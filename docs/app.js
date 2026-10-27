/**
 * FAIZIT INTERACTIVE TELEMETRY & SIMULATOR ENGINE
 * Production-ready UX • Haptic Responsiveness • Zero Dependencies
 */

document.addEventListener('DOMContentLoaded', () => {
  initNavbarScroll();
  initMobileMenu();
  initPhoneSimulator();
  initExtractionSandbox();
  initFaqAccordion();
  initCopyButtons();
});

/* ==========================================================================
   1. NAVBAR SCROLL EFFECT
   ========================================================================== */
function initNavbarScroll() {
  const navbar = document.querySelector('.nav-wrapper');
  if (!navbar) return;

  window.addEventListener('scroll', () => {
    if (window.scrollY > 40) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
  }, { passive: true });
}

/* ==========================================================================
   2. MOBILE NAVIGATION DRAWER
   ========================================================================== */
function initMobileMenu() {
  const toggleBtn = document.querySelector('.mobile-menu-toggle');
  const navLinks = document.querySelector('.nav-links');
  if (!toggleBtn || !navLinks) return;

  toggleBtn.addEventListener('click', () => {
    const isExpanded = toggleBtn.getAttribute('aria-expanded') === 'true';
    toggleBtn.setAttribute('aria-expanded', !isExpanded);
    
    if (!isExpanded) {
      navLinks.style.display = 'flex';
      navLinks.style.flexDirection = 'column';
      navLinks.style.position = 'absolute';
      navLinks.style.top = '100%';
      navLinks.style.left = '0';
      navLinks.style.right = '0';
      navLinks.style.background = 'rgba(8, 10, 15, 0.98)';
      navLinks.style.border = '1px solid rgba(255, 255, 255, 0.1)';
      navLinks.style.padding = '24px';
      navLinks.style.gap = '20px';
      toggleBtn.textContent = '✕ CLOSE';
    } else {
      navLinks.style.display = '';
      toggleBtn.textContent = '☰ MENU';
    }
  });

  // Close when clicking link
  navLinks.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => {
      if (window.innerWidth <= 768) {
        navLinks.style.display = '';
        toggleBtn.setAttribute('aria-expanded', 'false');
        toggleBtn.textContent = '☰ MENU';
      }
    });
  });
}

/* ==========================================================================
   3. INTERACTIVE SMARTPHONE MOCKUP & SHARE SHEET SIMULATOR
   ========================================================================== */
function initPhoneSimulator() {
  const shareBtn = document.getElementById('simShareTrigger');
  const sheet = document.getElementById('simBottomSheet');
  const formatButtons = document.querySelectorAll('.sim-format-btn');
  const downloadBtn = document.getElementById('simDownloadAction');
  const progressBox = document.getElementById('simProgressBox');
  const progressFill = document.getElementById('simProgressFill');
  const progressPct = document.getElementById('simProgressPercent');
  const progressSpeed = document.getElementById('simProgressSpeed');
  const sheetClose = document.getElementById('simSheetClose');
  const simMetaTitle = document.getElementById('simMetaTitle');
  const simMetaFormat = document.getElementById('simMetaFormat');

  let activeFormat = {
    id: '1080p',
    label: '1080p Full HD (60fps)',
    size: '42.8 MB',
    speed: '18.4 MB/s',
    isAudio: false
  };

  let isDownloading = false;

  // Format selection
  formatButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      if (isDownloading) return;
      formatButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      const formatId = btn.getAttribute('data-format');
      if (formatId === '1080p') {
        activeFormat = { id: '1080p', label: '1080p Full HD (60fps)', size: '42.8 MB', speed: '18.4 MB/s', isAudio: false };
      } else if (formatId === '720p') {
        activeFormat = { id: '720p', label: '720p HD (High Speed)', size: '19.2 MB', speed: '21.0 MB/s', isAudio: false };
      } else if (formatId === '480p') {
        activeFormat = { id: '480p', label: '480p SD (Data Saver)', size: '8.4 MB', speed: '24.2 MB/s', isAudio: false };
      } else if (formatId === 'audio') {
        activeFormat = { id: 'audio', label: 'Studio MP3 (320kbps Audio)', size: '5.1 MB', speed: '12.0 MB/s', isAudio: true };
      }

      if (simMetaFormat) {
        simMetaFormat.textContent = activeFormat.label;
      }
    });
  });

  // Trigger Share Sheet
  if (shareBtn && sheet) {
    shareBtn.addEventListener('click', () => {
      sheet.style.transform = 'translateY(0)';
      shareBtn.classList.add('share-active');
    });
  }

  // Close Sheet
  if (sheetClose && sheet) {
    sheetClose.addEventListener('click', () => {
      sheet.style.transform = 'translateY(100%)';
      if (shareBtn) shareBtn.classList.remove('share-active');
    });
  }

  // Download simulation
  if (downloadBtn && progressBox && progressFill && progressPct) {
    downloadBtn.addEventListener('click', () => {
      if (isDownloading) return;
      isDownloading = true;

      downloadBtn.disabled = true;
      downloadBtn.textContent = 'EXTRACTING STREAM...';
      progressBox.style.display = 'block';

      let currentPct = 0;
      progressFill.style.width = '0%';
      progressPct.textContent = '0%';

      const interval = setInterval(() => {
        currentPct += Math.floor(Math.random() * 12) + 8;
        if (currentPct > 100) currentPct = 100;

        progressFill.style.width = `${currentPct}%`;
        progressPct.textContent = `${currentPct}%`;

        if (progressSpeed) {
          const eta = Math.max(1, Math.ceil((100 - currentPct) / 30));
          progressSpeed.textContent = `${activeFormat.speed} • ETA: ${eta}s`;
        }

        if (currentPct >= 100) {
          clearInterval(interval);
          setTimeout(() => {
            downloadBtn.textContent = 'SAVED TO GALLERY ✅';
            downloadBtn.style.background = '#10B981';
            downloadBtn.style.borderColor = '#10B981';
            downloadBtn.style.color = '#FFFFFF';

            if (progressSpeed) {
              progressSpeed.textContent = `MediaStore: /Downloads/FaizIt/`;
            }

            setTimeout(() => {
              // Reset after 3 seconds
              isDownloading = false;
              downloadBtn.disabled = false;
              downloadBtn.textContent = 'DOWNLOAD NOW';
              downloadBtn.style.background = '';
              downloadBtn.style.borderColor = '';
              downloadBtn.style.color = '';
              progressBox.style.display = 'none';
            }, 3200);
          }, 300);
        }
      }, 180);
    });
  }
}

/* ==========================================================================
   4. INTERACTIVE EXTRACTION SANDBOX
   ========================================================================== */
function initExtractionSandbox() {
  const input = document.getElementById('sandboxInput');
  const trigger = document.getElementById('sandboxTrigger');
  const output = document.getElementById('sandboxOutput');
  const samplePills = document.querySelectorAll('.sample-tag');

  const mockDatabase = {
    instagram: {
      platform: "Instagram Reels",
      title: "Cyberpunk Architectural Aesthetics // Urban Motion",
      author: "@future.arch.lab",
      duration: "00:28",
      views: "1,420,890",
      resolution: "1080x1920 (9:16)",
      codec: "avc1.640028 (H.264 / AAC 48kHz)",
      audio_stream: "MP3 Studio Extractor Ready (320 kbps CBR)",
      file_size_estimate: "18.4 MB (Video) / 1.8 MB (Audio)"
    },
    youtube: {
      platform: "YouTube 4K Shorts",
      title: "Deep Neural Engine Architecture Walkthrough 2026",
      author: "Silicon Deep Dive",
      duration: "00:58",
      views: "4,891,200",
      resolution: "2160x3840 (4K UHD)",
      codec: "vp09.00.51.08 / Opus 160kbps",
      audio_stream: "FFmpeg Transcode to Pure MP3 320kbps",
      file_size_estimate: "54.2 MB (Full HD) / 4.2 MB (Audio)"
    },
    twitter: {
      platform: "X (Twitter) Video",
      title: "Starship Flight Test Multi-Angle Supersonic Landing",
      author: "@SpaceEngineering",
      duration: "01:14",
      views: "18,290,000",
      resolution: "1080x1080 (1:1)",
      codec: "H.264 High Profile / AAC-LC",
      audio_stream: "MP3 Audio Bitstream Lossless",
      file_size_estimate: "29.6 MB"
    },
    tiktok: {
      platform: "TikTok HD",
      title: "Minimalist Mechanical Keyboard Sound Test (Lubed Holy Pandas)",
      author: "@keebcraft",
      duration: "00:42",
      views: "890,400",
      resolution: "1080x1920",
      codec: "H.264 Baseline / AAC",
      audio_stream: "Clean Audio Extraction (Lossless)",
      file_size_estimate: "14.1 MB"
    }
  };

  function runExtraction(key = 'instagram') {
    if (!output) return;
    const data = mockDatabase[key] || mockDatabase.instagram;

    output.innerHTML = `
<div class="telemetry-code">
  <div style="color: #64748B; margin-bottom: 8px;">// FaizIt On-Device yt-dlp Extraction Telemetry</div>
  <div><span class="key">"target_platform"</span>: <span class="val">"${data.platform}"</span>,</div>
  <div><span class="key">"media_title"</span>: <span class="val">"${data.title}"</span>,</div>
  <div><span class="key">"creator"</span>: <span class="val">"${data.author}"</span>,</div>
  <div><span class="key">"duration"</span>: <span class="val">"${data.duration}"</span>,</div>
  <div><span class="key">"native_resolution"</span>: <span class="highlight">"${data.resolution}"</span>,</div>
  <div><span class="key">"primary_codec"</span>: <span class="val">"${data.codec}"</span>,</div>
  <div><span class="key">"audio_transcode"</span>: <span class="highlight">"${data.audio_stream}"</span>,</div>
  <div><span class="key">"est_payload_size"</span>: <span class="val">"${data.file_size_estimate}"</span>,</div>
  <div><span class="key">"storage_target"</span>: <span class="val">"/storage/emulated/0/Download/FaizIt/"</span>,</div>
  <div><span class="key">"security_status"</span>: <span class="highlight">"100% PRIVATE • ZERO LOGS"</span></div>
</div>
<div style="display: flex; flex-direction: column; justify-content: space-between; border-left: 1px solid rgba(255,255,255,0.08); padding-left: 20px;">
  <div>
    <div style="font-family: var(--font-mono); font-size: 11px; color: var(--accent-cyan); margin-bottom: 6px;">[ EXTRACTION VERIFIED ]</div>
    <div style="font-family: var(--font-display); font-size: 16px; font-weight: 700; color: #FFFFFF; margin-bottom: 8px;">Ready for 1-Tap Download</div>
    <p style="font-size: 12px; color: var(--text-secondary); line-height: 1.6;">Direct stream manifest parsed locally via bundled yt-dlp. No intermediary servers, zero cloud bandwidth cost.</p>
  </div>
  <div style="margin-top: 16px;">
    <a href="#download" class="btn-chamfer" style="padding: 10px 18px; font-size: 11px; width: 100%; text-align: center;">DOWNLOAD VIA FAIZIT APK</a>
  </div>
</div>
    `;
  }

  samplePills.forEach(pill => {
    pill.addEventListener('click', () => {
      const type = pill.getAttribute('data-type');
      if (input) {
        if (type === 'instagram') input.value = 'https://www.instagram.com/reel/C7K8j1q...';
        if (type === 'youtube') input.value = 'https://youtu.be/dQw4w9WgXcQ';
        if (type === 'twitter') input.value = 'https://x.com/tech_insider/status/1792837482...';
        if (type === 'tiktok') input.value = 'https://www.tiktok.com/@creator/video/738291...';
      }
      runExtraction(type);
    });
  });

  if (trigger && input) {
    trigger.addEventListener('click', () => {
      const val = input.value.toLowerCase();
      let key = 'instagram';
      if (val.includes('youtu')) key = 'youtube';
      else if (val.includes('x.com') || val.includes('twitter')) key = 'twitter';
      else if (val.includes('tiktok')) key = 'tiktok';
      runExtraction(key);
    });
  }

  // Initial run
  runExtraction('instagram');
}

/* ==========================================================================
   5. SHARP FAQ ACCORDION
   ========================================================================== */
function initFaqAccordion() {
  const faqItems = document.querySelectorAll('.faq-item');
  faqItems.forEach(item => {
    const question = item.querySelector('.faq-question');
    if (!question) return;

    question.addEventListener('click', () => {
      const wasActive = item.classList.contains('active');
      // Optional: Close others
      faqItems.forEach(i => i.classList.remove('active'));
      if (!wasActive) {
        item.classList.add('active');
      }
    });
  });
}

/* ==========================================================================
   6. COPY TO CLIPBOARD BUTTONS
   ========================================================================== */
function initCopyButtons() {
  const copyEmailBtn = document.getElementById('copyEmailBtn');
  if (copyEmailBtn) {
    copyEmailBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const email = 'mdfaizanhashmi3@gmail.com';
      navigator.clipboard.writeText(email).then(() => {
        const originalText = copyEmailBtn.innerHTML;
        copyEmailBtn.innerHTML = `<span>COPIED TO CLIPBOARD! ✅</span>`;
        copyEmailBtn.style.borderColor = 'var(--accent-cyan)';
        setTimeout(() => {
          copyEmailBtn.innerHTML = originalText;
          copyEmailBtn.style.borderColor = '';
        }, 2500);
      }).catch(() => {
        window.location.href = `mailto:${email}`;
      });
    });
  }
}
