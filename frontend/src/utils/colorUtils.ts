import type { CSSProperties } from "react";

export type ColorInfo = {
  dari: string;
  hex: string;
  background?: string;
};

type ColorDefinition = ColorInfo & {
  aliases: string[];
};

function normalizeColor(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/\u200c/g, "")
    .replace(/\s+/g, "")
    .replace(/-/g, "")
    .replace(/_/g, "");
}

const colors: ColorDefinition[] = [
  {
    dari: "سرخ",
    hex: "#dc2626",
    aliases: ["red", "سرخ", "قرمز"],
  },
  {
    dari: "سرخ روشن",
    hex: "#ef4444",
    aliases: ["scarlet", "lightred", "سرخروشن"],
  },
  {
    dari: "سرخ تیره",
    hex: "#991b1b",
    aliases: ["darkred", "crimson", "سرختیره"],
  },
  {
    dari: "اناری",
    hex: "#b91c1c",
    aliases: ["pomegranate", "anari", "اناری"],
  },
  {
    dari: "جگری",
    hex: "#7f1d1d",
    aliases: ["maroon", "جگری"],
  },
  {
    dari: "زرشکی",
    hex: "#800020",
    aliases: ["burgundy", "زرشکی"],
  },
  {
    dari: "عنابی",
    hex: "#9f1239",
    aliases: ["anabi", "عنابی"],
  },
  {
    dari: "شرابی",
    hex: "#881337",
    aliases: ["wine", "شرابی"],
  },
  {
    dari: "آلبالویی",
    hex: "#be123c",
    aliases: ["cherry", "آلبالویی"],
  },
  {
    dari: "یاقوتی",
    hex: "#9b111e",
    aliases: ["ruby", "یاقوتی"],
  },

  {
    dari: "صورتی",
    hex: "#ec4899",
    aliases: ["pink", "صورتی"],
  },
  {
    dari: "گلبهی",
    hex: "#fb7185",
    aliases: ["rose", "rosepink", "گلبهی"],
  },
  {
    dari: "کالباسی",
    hex: "#fda4af",
    aliases: ["salmonpink", "کالباسی"],
  },
  {
    dari: "صورتی چرک",
    hex: "#be7186",
    aliases: ["dustypink", "صورتیچرک"],
  },
  {
    dari: "سرخابی",
    hex: "#d946ef",
    aliases: ["fuchsia", "سرخابی"],
  },
  {
    dari: "ارغوانی",
    hex: "#c026d3",
    aliases: ["magenta", "ارغوانی"],
  },
  {
    dari: "صورتی روشن",
    hex: "#f9a8d4",
    aliases: ["lightpink", "صورتیروشن"],
  },
  {
    dari: "صورتی تیره",
    hex: "#db2777",
    aliases: ["darkpink", "صورتیتیره"],
  },

  {
    dari: "زرد",
    hex: "#facc15",
    aliases: ["yellow", "زرد"],
  },
  {
    dari: "خردلی",
    hex: "#ca8a04",
    aliases: ["mustard", "خردلی"],
  },
  {
    dari: "لیمویی",
    hex: "#eab308",
    aliases: ["lemon", "لیمویی"],
  },
  {
    dari: "زعفرانی",
    hex: "#f59e0b",
    aliases: ["saffron", "زعفرانی"],
  },
  {
    dari: "کهربایی",
    hex: "#d97706",
    aliases: ["amber", "کهربایی"],
  },
  {
    dari: "طلایی",
    hex: "#d4a017",
    aliases: ["gold", "golden", "طلایی"],
  },
  {
    dari: "نباتی",
    hex: "#fde68a",
    aliases: ["rockcandy", "نباتی"],
  },

  {
    dari: "نارنجی",
    hex: "#f97316",
    aliases: ["orange", "نارنجی"],
  },
  {
    dari: "نارنجی سوخته",
    hex: "#c2410c",
    aliases: ["burntorange", "نارنجیسوخته"],
  },
  {
    dari: "هلویی",
    hex: "#fb923c",
    aliases: ["peach", "هلویی"],
  },
  {
    dari: "آجری",
    hex: "#b45309",
    aliases: ["terracotta", "brick", "آجری"],
  },
  {
    dari: "سفالی",
    hex: "#c2410c",
    aliases: ["clay", "سفالی"],
  },

  {
    dari: "سبز",
    hex: "#16a34a",
    aliases: ["green", "سبز"],
  },
  {
    dari: "سبز یشمی",
    hex: "#047857",
    aliases: ["jadegreen", "سبزیشمی"],
  },
  {
    dari: "یشمی",
    hex: "#059669",
    aliases: ["jade", "یشمی"],
  },
  {
    dari: "زنگالی",
    hex: "#2f5d50",
    aliases: ["forestgreen", "zangali", "زنگالی"],
  },
  {
    dari: "زیتونی",
    hex: "#808000",
    aliases: ["olive", "زیتونی"],
  },
  {
    dari: "سدری",
    hex: "#65a30d",
    aliases: ["cedar", "سدری"],
  },
  {
    dari: "پسته‌ای",
    hex: "#84cc16",
    aliases: ["pistachio", "پستهای", "پسته‌ای"],
  },
  {
    dari: "نعنایی",
    hex: "#6ee7b7",
    aliases: ["mint", "نعنایی"],
  },
  {
    dari: "ارتشی",
    hex: "#4d7c0f",
    aliases: ["army", "militarygreen", "ارتشی"],
  },
  {
    dari: "سبز روشن",
    hex: "#4ade80",
    aliases: ["lightgreen", "سبزروشن"],
  },
  {
    dari: "سبز تیره",
    hex: "#166534",
    aliases: ["darkgreen", "سبزتیره"],
  },

  {
    dari: "آبی",
    hex: "#2563eb",
    aliases: ["blue", "آبی"],
  },
  {
    dari: "آبی آسمانی",
    hex: "#38bdf8",
    aliases: ["skyblue", "آبیآسمانی"],
  },
  {
    dari: "لاجوردی",
    hex: "#1d4ed8",
    aliases: ["azure", "lapis", "لاجوردی"],
  },
  {
    dari: "نیلی",
    hex: "#4338ca",
    aliases: ["indigo", "نیلی"],
  },
  {
    dari: "کبود",
    hex: "#1e40af",
    aliases: ["kabood", "کبود"],
  },
  {
    dari: "فیروزه‌ای",
    hex: "#14b8a6",
    aliases: ["turquoise", "aqua", "فیروزهای"],
  },
  {
    dari: "سورمه‌ای",
    hex: "#172554",
    aliases: ["navy", "surme", "سورمهای", "سرمهای"],
  },
  {
    dari: "سرمه‌ای",
    hex: "#172554",
    aliases: ["سرمه‌ای", "سرمهی"],
  },
  {
    dari: "آبی نفتی",
    hex: "#115e59",
    aliases: ["petrolblue", "آبینفتی"],
  },
  {
    dari: "آبی روشن",
    hex: "#60a5fa",
    aliases: ["lightblue", "آبیروشن"],
  },
  {
    dari: "آبی تیره",
    hex: "#1e3a8a",
    aliases: ["darkblue", "آبیتیره"],
  },

  {
    dari: "بنفش",
    hex: "#9333ea",
    aliases: ["purple", "بنفش"],
  },
  {
    dari: "یاسی",
    hex: "#c4b5fd",
    aliases: ["lavender", "lilac", "یاسی"],
  },
  {
    dari: "آلویی",
    hex: "#7e22ce",
    aliases: ["plum", "آلویی"],
  },
  {
    dari: "بادمجانی",
    hex: "#581c87",
    aliases: ["eggplant", "بادنجانی", "بادمجانی"],
  },

  {
    dari: "نصواری",
    hex: "#92400e",
    aliases: ["brown", "نصواری"],
  },
  {
    dari: "قهوه‌ای",
    hex: "#6f4e37",
    aliases: ["coffee", "cafe", "قهوهای", "قهوه‌ای"],
  },
  {
    dari: "شکلاتی",
    hex: "#7c2d12",
    aliases: ["chocolate", "شکلاتی"],
  },
  {
    dari: "چاکلیتی",
    hex: "#7b3f00",
    aliases: ["chocolaty", "چاکلیتی"],
  },
  {
    dari: "نسکافه‌ای",
    hex: "#a16207",
    aliases: ["nescafe", "نسکافهای"],
  },
  {
    dari: "فندقی",
    hex: "#92400e",
    aliases: ["hazelnut", "فندقی"],
  },
  {
    dari: "شتری",
    hex: "#c08457",
    aliases: ["camel", "شتری"],
  },
  {
    dari: "خاکی",
    hex: "#a16207",
    aliases: ["khaki", "خاکی"],
  },
  {
    dari: "کاراملی",
    hex: "#b45309",
    aliases: ["caramel", "کاراملی"],
  },

  {
    dari: "سفید",
    hex: "#f8fafc",
    aliases: ["white", "سفید"],
  },
  {
    dari: "شیری",
    hex: "#fff7ed",
    aliases: ["milk", "شیری"],
  },
  {
    dari: "استخوانی",
    hex: "#f5f5dc",
    aliases: ["offwhite", "bone", "استخوانی"],
  },
  {
    dari: "عاجی",
    hex: "#fffff0",
    aliases: ["ivory", "عاجی"],
  },
  {
    dari: "کرمی",
    hex: "#f5deb3",
    aliases: ["cream", "کرمی"],
  },
  {
    dari: "کریمی",
    hex: "#fffdd0",
    aliases: ["creamy", "کریمی"],
  },
  {
    dari: "یاسمندی",
    hex: "#f8f4e3",
    aliases: ["jasmine", "jasminewhite", "یاسمندی"],
  },
  {
    dari: "کرم",
    hex: "#ded0bc",
    aliases: ["beigecream", "کرم"],
  },
  {
    dari: "بژ",
    hex: "#d6c3a5",
    aliases: ["beige", "بژ"],
  },
  {
    dari: "صدفی",
    hex: "#fff5ee",
    aliases: ["pearl", "صدفی"],
  },

  {
    dari: "سیاه",
    hex: "#111827",
    aliases: ["black", "سیاه", "مشکی"],
  },
  {
    dari: "زغالی",
    hex: "#374151",
    aliases: ["charcoal", "زغالی"],
  },
  {
    dari: "دودی",
    hex: "#4b5563",
    aliases: ["smoke", "دودی"],
  },
  {
    dari: "سربی",
    hex: "#475569",
    aliases: ["lead", "سربی"],
  },

  {
    dari: "خاکستری",
    hex: "#6b7280",
    aliases: ["gray", "grey", "خاکستری"],
  },
  {
    dari: "طوسی",
    hex: "#9ca3af",
    aliases: ["toshi", "طوسی"],
  },
  {
    dari: "موشی",
    hex: "#737373",
    aliases: ["mousegray", "موشی"],
  },
  {
    dari: "خاکستری روشن",
    hex: "#d1d5db",
    aliases: ["lightgray", "خاکستریروشن"],
  },
  {
    dari: "نقره‌ای",
    hex: "#cbd5e1",
    aliases: ["silver", "نقرهای"],
  },

  {
    dari: "مسی",
    hex: "#b87333",
    aliases: ["copper", "مسی"],
  },
  {
    dari: "برنزی",
    hex: "#cd7f32",
    aliases: ["bronze", "برنزی"],
  },
  {
    dari: "رزگلد",
    hex: "#b76e79",
    aliases: ["rosegold", "رزگلد"],
  },
  {
    dari: "شامپاینی",
    hex: "#f7e7ce",
    aliases: ["champagne", "شامپاینی"],
  },
  {
    dari: "فلزی",
    hex: "#94a3b8",
    aliases: ["metallic", "فلزی"],
  },

  {
    dari: "چندرنگ",
    hex: "#64748b",
    background:
      "linear-gradient(135deg,#ef4444,#facc15,#22c55e,#3b82f6,#9333ea)",
    aliases: ["multicolor", "multicolored", "چندرنگ"],
  },
  {
    dari: "رنگارنگ",
    hex: "#64748b",
    background:
      "linear-gradient(135deg,#ec4899,#f97316,#22c55e,#2563eb)",
    aliases: ["rainbow", "colorful", "رنگارنگ"],
  },
  {
    dari: "گل‌دار",
    hex: "#be123c",
    background:
      "radial-gradient(circle,#fda4af 18%,#be123c 20%,#fda4af 38%,#166534 40%)",
    aliases: ["floral", "گلدار", "گل‌دار"],
  },
  {
    dari: "راه‌راه",
    hex: "#64748b",
    background:
      "repeating-linear-gradient(45deg,#64748b 0 6px,#e5e7eb 6px 12px)",
    aliases: ["striped", "راهراه", "راه‌راه"],
  },
];

const aliasMap = new Map<string, ColorInfo>();

for (const color of colors) {
  const info: ColorInfo = {
    dari: color.dari,
    hex: color.hex,
    background: color.background,
  };

  for (const alias of color.aliases) {
    aliasMap.set(normalizeColor(alias), info);
  }
}

export function getColorInfo(
  value: string
): ColorInfo | undefined {
  if (!value || !value.trim()) {
    return undefined;
  }

  return aliasMap.get(normalizeColor(value));
}

export function translateColor(
  value: string,
  isDari: boolean
): string {
  if (!value || !value.trim()) {
    return "—";
  }

  const info = getColorInfo(value);

  if (!info || !isDari) {
    return value;
  }

  return info.dari;
}

export function getColorStyle(
  value: string
): CSSProperties {
  const info = getColorInfo(value);

  if (!info) {
    return {
      backgroundColor: "#d1d5db",
    };
  }

  if (info.background) {
    return {
      background: info.background,
    };
  }

  return {
    backgroundColor: info.hex,
  };
}