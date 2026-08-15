import type { PackingItem } from './model';

type PackingModalProps = {
  isOpen: boolean;
  items: PackingItem[];
  secondaryButtonClassName: string;
  onClose: () => void;
  onItemChange: (index: number, field: 'item' | 'required', value: string | boolean) => void;
  onItemRemove: (index: number) => void;
  onItemAdd: () => void;
};

export const PackingModal = ({
  isOpen,
  items,
  secondaryButtonClassName,
  onClose,
  onItemChange,
  onItemRemove,
  onItemAdd,
}: PackingModalProps) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button type="button" aria-label="준비물 창 닫기" onClick={onClose} className="absolute inset-0 bg-slate-950/80 backdrop-blur-sm" />
      <div className="relative z-10 w-full max-w-xl rounded-[28px] border border-white/10 bg-slate-950 p-5 shadow-2xl md:p-6">
        <div className="flex items-start justify-between gap-4">
          <div><p className="text-xs font-bold tracking-[0.22em] text-indigo-300">PACKING LIST</p><h2 className="mt-2 text-2xl font-bold text-white">여행 준비물</h2></div>
          <button type="button" onClick={onClose} className="rounded-xl border border-white/10 px-3 py-2 text-slate-400 transition hover:bg-white/10 hover:text-white"><i className="fa-solid fa-xmark" /></button>
        </div>
        <div className="mt-5 max-h-[55vh] space-y-2 overflow-y-auto pr-1">
          {items.map((item, index) => (
            <div key={item.id} className="grid grid-cols-[auto_1fr_auto] items-center gap-3 rounded-xl border border-white/10 bg-white/[0.04] px-3 py-2.5">
              <label className="flex cursor-pointer items-center gap-2"><input type="checkbox" checked={item.required} onChange={(event) => onItemChange(index, 'required', event.target.checked)} className="h-4 w-4 rounded border-white/20 bg-transparent text-indigo-500 focus:ring-indigo-500" /><span className="text-xs text-slate-400">필수</span></label>
              <input value={item.item} onChange={(event) => onItemChange(index, 'item', event.target.value)} className="min-w-0 bg-transparent text-sm text-white outline-none placeholder:text-slate-600" placeholder="예: 여권" maxLength={100} />
              <button type="button" onClick={() => onItemRemove(index)} aria-label="준비물 삭제" className="p-1.5 text-slate-500 transition hover:text-rose-300"><i className="fa-solid fa-trash" /></button>
            </div>
          ))}
        </div>
        <div className="mt-5 flex flex-wrap gap-3"><button type="button" onClick={onItemAdd} className={secondaryButtonClassName}><i className="fa-solid fa-plus" />준비물 추가</button><button type="button" onClick={onClose} className="theme-btn-primary px-5 py-2.5">완료</button></div>
      </div>
    </div>
  );
};
