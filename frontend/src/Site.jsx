import { useEffect, useState } from 'react'
import App from './App'
import WelcomeSite from './welcome/WelcomeSite'

// Hash routes keep this Vite app runnable without server rewrite rules.
export default function Site() {
  const [preview, setPreview] = useState(() => location.hash === '#preview')
  const [visitor, setVisitor] = useState(null)
  useEffect(() => {
    const sync = () => setPreview(location.hash === '#preview')
    addEventListener('hashchange', sync)
    return () => removeEventListener('hashchange', sync)
  }, [])
  if (preview) return <><nav className="existing-shell-nav" dir="rtl"><button onClick={() => { location.hash = 'home' }}>العودة للترحيب</button><span>{visitor ? `الحساب رقم ${visitor.id}` : 'معاينة الواجهة الحالية · المستخدم التجريبي 1'}</span></nav><App userId={visitor?.id ?? 1} /></>
  return <WelcomeSite onPreview={() => { location.hash = 'preview' }} onVisitorLogin={account => { setVisitor(account); location.hash = 'preview' }} />
}
