import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

interface CustomCalendarProps {
  value: string;
  onChange: (dateStr: string) => void;
  placeholder?: string;
}

export const CustomCalendar = ({ value, onChange, placeholder }: CustomCalendarProps) => {
  const selectedDate = value ? new Date(value) : null;

  const handleDateChange = (date: Date | null) => {
    if (date) {
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      onChange(`${y}-${m}-${d}`);
    } else {
      onChange('');
    }
  };

  return (
    <div className="relative w-full datepicker-theme-wrapper">
      <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500 z-10 pointer-events-none">
        <i className="fa-regular fa-calendar"></i>
      </span>
      <DatePicker
        selected={selectedDate}
        onChange={handleDateChange}
        dateFormat="yyyy-MM-dd"
        maxDate={new Date()}
        showMonthDropdown
        showYearDropdown
        dropdownMode="select"
        placeholderText={placeholder || "생년월일을 선택해 주세요"}
        className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-10 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition cursor-pointer"
      />
    </div>
  );
};
