import type {
  ExpenseCategory,
  ExpenseOption,
} from "../types/expense";

type ExpenseTypeSelectProps = {
  value: ExpenseCategory;
  options: ExpenseOption[];
  onChange: (value: ExpenseCategory) => void;
  isDari?: boolean;
};

export default function ExpenseTypeSelect({
  value,
  options,
  onChange,
  isDari = true,
}: ExpenseTypeSelectProps) {
  return (
    <div>
      <label
        htmlFor="expense-category"
        className="mb-2 block text-base font-bold text-[#173f38]"
      >
        {isDari ? "نوع مصرف" : "Expense Type"}

        <span className="mr-1 text-[#b42318]">
          *
        </span>
      </label>

      <select
        id="expense-category"
        value={value}
        onChange={(event) =>
          onChange(
            event.target.value as ExpenseCategory
          )
        }
        className="form-input"
        required
      >
        {options.map((option) => (
          <option
            key={option.value}
            value={option.value}
          >
            {isDari ? option.dari : option.english}
          </option>
        ))}
      </select>
    </div>
  );
}