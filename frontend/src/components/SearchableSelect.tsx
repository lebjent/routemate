import { useEffect, useState } from 'react';

export type SearchOption = {
  value: string;
  label: string;
  hint?: string;
};

type SearchableSelectProps = {
  value: string;
  options: SearchOption[];
  placeholder: string;
  disabled?: boolean;
  onChange: (value: string) => void;
};

/** 검색 가능한 선택 입력입니다. 일정·관리자·파트너 화면에서 공통으로 사용합니다. */
export const SearchableSelect = ({
  value,
  options,
  placeholder,
  disabled = false,
  onChange,
}: SearchableSelectProps) => {
  const selected = options.find((option) => option.value === value);
  const [query, setQuery] = useState(selected?.label || '');
  const [open, setOpen] = useState(false);

  useEffect(() => {
    setQuery(selected?.label || '');
  }, [selected?.label, value]);

  const filteredOptions = options.filter((option) =>
    `${option.label} ${option.value} ${option.hint || ''}`.toLowerCase().includes(query.trim().toLowerCase())
  );

  return (
    <div className="relative min-w-0">
      <input
        value={query}
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
          if (!event.target.value) onChange('');
        }}
        onFocus={() => setOpen(true)}
        onKeyDown={(event) => {
          if (event.key === 'Enter' && filteredOptions.length > 0) {
            event.preventDefault();
            const firstOption = filteredOptions[0];
            setQuery(firstOption.label);
            setOpen(false);
            onChange(firstOption.value);
          }
        }}
        onBlur={() => window.setTimeout(() => setOpen(false), 120)}
        className={`min-w-0 w-full rounded-xl border border-white/10 bg-slate-950/55 px-3.5 py-3 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400/60 ${disabled ? 'cursor-not-allowed opacity-60' : ''}`}
        placeholder={placeholder}
        disabled={disabled}
        data-searchable-select="true"
        autoComplete="off"
      />
      {open && !disabled ? (
        <div className="absolute left-0 right-0 top-full z-30 mt-1 max-h-60 overflow-y-auto rounded-xl border border-white/10 bg-slate-900 p-1 shadow-2xl">
          {filteredOptions.length > 0 ? filteredOptions.map((option) => (
            <button
              key={option.value}
              type="button"
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => {
                setQuery(option.label);
                setOpen(false);
                onChange(option.value);
              }}
              className="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-sm text-slate-200 hover:bg-indigo-500/20"
            >
              <span>{option.label}</span>
              {option.hint ? <span className="ml-2 text-xs text-slate-500">{option.hint}</span> : null}
            </button>
          )) : <p className="px-3 py-3 text-sm text-slate-500">검색 결과가 없습니다.</p>}
        </div>
      ) : null}
    </div>
  );
};
