let cbSeq = 0

export function isKSU() {
  return typeof ksu !== 'undefined' && typeof ksu.exec === 'function'
}

export function execCommand(cmd, timeoutMs = 5000) {
  return new Promise((resolve, reject) => {
    if (typeof ksu !== 'undefined' && typeof ksu.exec === 'function') {
      const id = `_fpsm_${++cbSeq}_${Date.now()}`

      const timer = setTimeout(() => {
        if (window[id]) {
          delete window[id]
          resolve('')
        }
      }, timeoutMs)

      window[id] = (errno, stdout, stderr) => {
        clearTimeout(timer)
        delete window[id]
        resolve(stdout || stderr || '')
      }

      try {
        ksu.exec(cmd, '{}', id)
      } catch (e) {
        clearTimeout(timer)
        delete window[id]
        reject(e)
      }
    } else if (typeof exec === 'function') {
      exec(cmd)
        .then(r => resolve(typeof r === 'object' ? (r.stdout || r.stderr || '') : String(r)))
        .catch(reject)
    } else {
      /* Browser fallback */
      resolve('')
    }
  })
}

export async function readStateFile(filename) {
  const fullPath = `/data/adb/modules/fps_moon/state/${filename}`
  try {
    const out = await execCommand(`cat ${fullPath} 2>/dev/null`)
    if (out && out.trim().startsWith('{')) {
      return JSON.parse(out.trim())
    }
  } catch (e) {}

  // Fallback to fetch if running under local server or preview
  try {
    const res = await fetch(`state/${filename}`)
    if (res.ok) {
      return await res.json()
    }
  } catch (e) {}

  return null
}

export async function writeStateFile(filename, data) {
  const fullPath = `/data/adb/modules/fps_moon/state/${filename}`
  const jsonStr = typeof data === 'string' ? data : JSON.stringify(data)
  const escaped = jsonStr.replace(/'/g, "'\\''")
  return execCommand(`mkdir -p /data/adb/modules/fps_moon/state && echo '${escaped}' > ${fullPath}`)
}

export function sanitize(s) {
  return String(s).replace(/[^a-zA-Z0-9._:\-]/g, '').trim()
}

export async function openExternal(url) {
  if (!url) return
  const cleanUrl = String(url).trim()

  try {
    if (typeof ksu !== 'undefined' && typeof ksu.open === 'function') {
      ksu.open(cleanUrl)
      return
    }
  } catch (e) {}

  try {
    const res = await execCommand(`am start -a android.intent.action.VIEW -d "${cleanUrl}" 2>&1`)
    if (res && res.includes('Starting: Intent')) {
      return
    }
  } catch (e) {}

  try {
    const a = document.createElement('a')
    a.href = cleanUrl
    a.target = '_blank'
    a.rel = 'noopener noreferrer'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } catch (e) {}
}
