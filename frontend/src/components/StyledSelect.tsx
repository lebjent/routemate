import { useEffect, useRef, useState } from 'react';

/** 스타일 선택 상자에 제공할 옵션의 값·표시명·선택 아이콘이다. */
export type StyledSelectOption = {
  value: string;
  label: string;
  description?: string;
  icon?: string;
};

/** 프로젝트 공통 스타일 선택 상자의 제어형 입력 속성이다. */
type StyledSelectProps = {
  value: string;
  options: StyledSelectOption[];
  onChange: (value: string) => void;
  placeholder?: string;
  ariaLabel?: string;
  disabled?: boolean;
  required?: boolean;
  className?: string;
};

/**
 * 기본 select의 접근성은 유지하면서 RouteMate 스타일을 적용한 선택 컴포넌트다.
 */
export const StyledSelect = ({
  value,
  options,
  onChange,
  placeholder = '선택하세요',
  ariaLabel,
  disabled = false,
  required = false,
  className = '',
}: StyledSelectProps) => {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const selected = options.find((option) => option.value === value);

  useEffect(() => {
    const handlePointerDown = (event: PointerEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) setOpen(false);
    };
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false);
    };
    document.addEventListener('pointerdown', handlePointerDown);
    window.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, []);

  return (
    <div ref={containerRef} className="relative min-w-0">
      {required ? <input tabIndex={-1} aria-hidden="true" readOnly required value={value} onChange={() => undefined} className="pointer-events-none absolute h-px w-px opacity-0" /> : null}
      <button
        type="button"
        disabled={disabled}
        aria-label={ariaLabel}
        aria-haspopup="listbox"
        aria-expanded={open}
        onClick={() => setOpen((current) => !current)}
        className={`flex h-11 w-full items-center justify-between gap-3 rounded-xl border px-3.5 text-sm outline-none transition focus:ring-4 focus:ring-indigo-400/10 disabled:cursor-not-allowed disabled:opacity-50 ${open ? 'border-indigo-300/70 bg-indigo-500/10 text-indigo-100' : 'border-white/10 bg-slate-950/70 text-slate-300 hover:border-indigo-400/40'} ${className}`}
      >
        <span className="flex min-w-0 items-center gap-2.5">
          <span className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-lg ${selected ? 'bg-indigo-400/10 text-indigo-300' : 'bg-white/5 text-slate-600'}`}>
            <i className={`fa-solid ${selected?.icon ?? 'fa-list'} text-[10px]`} aria-hidden="true" />
          </span>
          <span className="truncate">{selected?.label ?? placeholder}</span>
        </span>
        <i className={`fa-solid fa-chevron-down shrink-0 text-[10px] text-indigo-300 transition ${open ? 'rotate-180' : ''}`} aria-hidden="true" />
      </button>
      {open ? (
        <div role="listbox" aria-label={ariaLabel} className="absolute left-0 right-0 top-full z-50 mt-2 max-h-64 overflow-y-auto rounded-2xl border border-indigo-400/30 bg-slate-900 p-1.5 shadow-2xl shadow-black/50 ring-1 ring-white/5">
          {options.map((option) => (
            <button
              type="button"
              role="option"
              aria-selected={value === option.value}
              key={option.value}
              onClick={() => { onChange(option.value); setOpen(false); }}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition ${value === option.value ? 'bg-indigo-500/15 text-indigo-100' : 'text-slate-300 hover:bg-white/[0.06] hover:text-white'}`}
            >
              <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${value === option.value ? 'bg-indigo-400/15 text-indigo-200' : 'bg-white/5 text-slate-500'}`}>
                <i className={`fa-solid ${option.icon ?? 'fa-list'} text-[10px]`} aria-hidden="true" />
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate text-xs font-bold">{option.label}</span>
                {option.description ? <span className="mt-0.5 block truncate text-[10px] font-normal text-slate-500">{option.description}</span> : null}
              </span>
              {value === option.value ? <i className="fa-solid fa-check text-xs text-indigo-300" aria-hidden="true" /> : null}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
};
