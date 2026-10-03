import { useEffect, useRef, useState } from 'react'
import { documentApi } from '../api/services'
import { useAuth } from '../context/auth'
import { errMsg } from '../utils/format'
import { btn } from '../utils/styles'
import { Alert } from './ui'

const MAX_MB = 5
const ACCEPT = '.pdf,.jpg,.jpeg,.png'

const formatSize = (bytes) => (bytes > 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.ceil(bytes / 1024)} KB`)

/** Lists, uploads and downloads medical files of one appointment (stored in MongoDB GridFS). */
export default function DocumentsPanel({ appointmentId }) {
  const { user } = useAuth()
  const inputRef = useRef(null)
  const [docs, setDocs] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [reload, setReload] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    documentApi
      .list(appointmentId)
      .then(setDocs)
      .catch((e) => setError(errMsg(e)))
      .finally(() => setLoading(false))
  }, [appointmentId, reload])

  const onFile = async (e) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    if (file.size > MAX_MB * 1024 * 1024) return setError(`File is larger than ${MAX_MB} MB.`)
    setError('')
    setBusy(true)
    try {
      await documentApi.upload(appointmentId, file)
      setReload((r) => r + 1)
    } catch (err) {
      setError(errMsg(err))
    } finally {
      setBusy(false)
    }
  }

  const onDownload = async (doc) => {
    setError('')
    try {
      const blob = await documentApi.download(doc.id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = doc.fileName
      a.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      setError(errMsg(err))
    }
  }

  const onDelete = async (doc) => {
    if (!window.confirm(`Delete ${doc.fileName}?`)) return
    try {
      await documentApi.remove(doc.id)
      setReload((r) => r + 1)
    } catch (err) {
      setError(errMsg(err))
    }
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-left sm:p-4">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm font-semibold text-slate-800">Medical files</p>
        <input ref={inputRef} type="file" accept={ACCEPT} onChange={onFile} className="hidden" />
        <button onClick={() => inputRef.current?.click()} disabled={busy} className={`${btn.secondary} px-3 py-1.5`}>
          {busy ? 'Uploading…' : '+ Upload file'}
        </button>
      </div>

      <Alert>{error}</Alert>

      {loading ? (
        <p className="text-sm text-slate-500">Loading…</p>
      ) : docs.length === 0 ? (
        <p className="text-sm text-slate-500">No files yet. PDF, JPG or PNG up to {MAX_MB} MB.</p>
      ) : (
        <ul className="divide-y divide-slate-200">
          {docs.map((d) => (
            <li key={d.id} className="flex flex-wrap items-center justify-between gap-2 py-2 text-sm">
              <div className="min-w-0">
                <p className="truncate font-medium text-slate-800">{d.fileName}</p>
                <p className="text-xs text-slate-500">
                  {formatSize(d.size)} · by {d.uploaderName} · {new Date(d.uploadedAt).toLocaleDateString('en-GB')}
                </p>
              </div>
              <div className="flex gap-3">
                <button onClick={() => onDownload(d)} className="cursor-pointer text-xs font-medium text-teal-700 hover:underline">
                  Download
                </button>
                {d.uploadedBy === user?.id && (
                  <button onClick={() => onDelete(d)} className="cursor-pointer text-xs font-medium text-red-600 hover:underline">
                    Delete
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
