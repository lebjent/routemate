import { useState } from 'react';
import axios from 'axios';

type Props = { value: string; endpoint: string; onChange: (url: string) => void; onUploadingChange: (value: boolean) => void; disabled?: boolean; label?: string };

export const ImageUpload = ({ value, endpoint, onChange, onUploadingChange, disabled, label = '대표 이미지' }: Props) => {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  return <div className="grid gap-2 text-sm text-slate-300">
    <label className="grid gap-2">{label}<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" disabled={disabled || uploading}
      className="w-full rounded-xl border border-dashed border-indigo-400/30 p-3 text-xs file:mr-3 file:rounded-lg file:border-0 file:bg-indigo-500/20 file:px-3 file:py-2 file:text-indigo-200"
      onChange={async (event) => {
        const input = event.currentTarget; const file = input.files?.[0]; if (!file) return;
        setError('');
        if (file.size > 10 * 1024 * 1024) { setError('이미지는 10MB 이하만 업로드할 수 있습니다.'); input.value = ''; return; }
        setUploading(true); onUploadingChange(true);
        try {
          const data = new FormData(); data.append('file', file);
          const response = await axios.post<{ imageUrl: string }>(endpoint, data); onChange(response.data.imageUrl);
        } catch (failure) { setError(axios.isAxiosError(failure) ? failure.response?.data?.detail || '이미지를 업로드하지 못했습니다.' : '이미지를 업로드하지 못했습니다.'); }
        finally { setUploading(false); onUploadingChange(false); input.value = ''; }
      }} /></label>
    <span className="text-xs text-slate-500" aria-live="polite">{uploading ? '이미지 업로드 중…' : 'JPG, PNG, WEBP, GIF · 최대 10MB'}</span>
    {error && <p role="alert" className="text-xs text-rose-300">{error}</p>}
    {value && <><img src={value} alt={`${label} 미리보기`} className="h-36 w-full rounded-xl object-contain" /><button type="button" disabled={disabled || uploading} onClick={() => onChange('')} className="justify-self-start text-xs text-slate-400">이미지 선택 해제</button></>}
  </div>;
};
