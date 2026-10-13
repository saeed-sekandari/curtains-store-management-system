import {
  useEffect,
  useMemo,
  useState,
} from "react";

import type { InventoryProduct } from "../types/inventory";

import {
  getColorStyle,
  translateColor,
} from "../utils/colorUtils";

type InventoryTableProps = {
  products: InventoryProduct[];
  storeCode: string;
  isDari?: boolean;
};

function formatMeters(
  value: number,
  isDari: boolean
) {
  return `${value.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${isDari ? "متر" : "m"}`;
}

function getStatus(
  product: InventoryProduct,
  isDari: boolean
) {
  if (product.availableMeterage <= 0) {
    return {
      label: isDari ? "تمام‌شده" : "Out",
      className: "bg-[#fff0f0] text-[#d42626]",
    };
  }

  if (product.availableMeterage <= 15) {
    return {
      label: isDari ? "موجودی کم" : "Low",
      className: "bg-[#fff8e8] text-[#a56a00]",
    };
  }

  return {
    label: isDari ? "موجود" : "Available",
    className: "bg-[#e9f8ed] text-[#18733a]",
  };
}

export default function InventoryTable({
  products,
  storeCode,
  isDari = true,
}: InventoryTableProps) {
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);

  const totalPages = Math.max(
    1,
    Math.ceil(products.length / pageSize)
  );

  useEffect(() => {
    setCurrentPage(1);
  }, [products, pageSize]);

  const visibleProducts = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;

    return products.slice(start, end);
  }, [products, currentPage, pageSize]);

  const firstProduct =
    products.length === 0
      ? 0
      : (currentPage - 1) * pageSize + 1;

  const lastProduct = Math.min(
    currentPage * pageSize,
    products.length
  );

  const pageNumbers = Array.from(
    { length: totalPages },
    (_, index) => index + 1
  );

  return (
    <div className="overflow-hidden rounded-xl border border-[#e4e4e4] bg-white">
      <div className="overflow-x-auto">
        <table className="min-w-[1050px] w-full border-collapse">
          <thead className="bg-[#f7f7f7] text-sm text-[#222]">
            <tr>
              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "نام محصول" : "Product Name"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "رنگ" : "Color"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "کد محصول" : "Product Code"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "متر اصلی" : "Original Meters"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "متر موجود" : "Available Meters"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "محل نگهداری" : "Storage Location"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "وضعیت" : "Status"}
              </th>

              <th className="px-5 py-4 text-right font-bold">
                {isDari ? "عملیات" : "Actions"}
              </th>
            </tr>
          </thead>

          <tbody>
            {visibleProducts.map((product) => {
              const status = getStatus(product, isDari);

              return (
                <tr
                  key={product.id}
                  className="border-t border-[#eeeeee] transition hover:bg-[#fafafa]"
                >
                  <td className="px-5 py-4 font-semibold text-[#111]">
                    {product.productName}
                  </td>

                  <td className="px-5 py-4 text-[#333]">
                    <span className="flex items-center gap-3">
                      <span
                        className="h-5 w-5 rounded-full border border-black/10"
                        style={getColorStyle(
                          product.color || ""
                        )}
                      />

                      <span>
                        {translateColor(
                          product.color || "",
                          isDari
                        )}
                      </span>
                    </span>
                  </td>

                  <td className="px-5 py-4 text-[#333]">
                    {product.productCode}
                  </td>

                  <td className="px-5 py-4 text-[#333]">
                    {product.originalMeterage > 0
                      ? formatMeters(
                          product.originalMeterage,
                          isDari
                        )
                      : "—"}
                  </td>

                  <td className="px-5 py-4 font-semibold text-[#173f38]">
                    {formatMeters(
                      product.availableMeterage,
                      isDari
                    )}
                  </td>

                  <td className="px-5 py-4 text-[#444]">
                    {product.location || "—"}
                  </td>

                  <td className="px-5 py-4">
                    <span
                      className={`inline-flex min-w-[100px] justify-center rounded-lg px-3 py-2 text-sm font-bold ${status.className}`}
                    >
                      {status.label}
                    </span>
                  </td>

                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        className="flex h-9 w-9 items-center justify-center rounded-full bg-[#f4f4f4] text-lg"
                      >
                        ...
                      </button>

                      <a
                        href={`/inventory/${product.id}/edit?store=${storeCode}`}
                        className="flex h-9 w-9 items-center justify-center rounded-full bg-[#f4f4f4] text-lg"
                      >
                        ✎
                      </a>

                      <button
                        type="button"
                        className="flex h-9 w-9 items-center justify-center rounded-full bg-[#fff0f0] text-lg text-red-600"
                      >
                        ♜
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between border-t border-[#eeeeee] px-5 py-4 text-sm text-[#555]">
        <span>
          {isDari
            ? `نمایش ${firstProduct} تا ${lastProduct} از ${products.length} محصول`
            : `Showing ${firstProduct} to ${lastProduct} of ${products.length} products`}
        </span>

        <div className="flex items-center gap-2">
          <button
            type="button"
            disabled={currentPage === 1}
            onClick={() =>
              setCurrentPage((page) =>
                Math.max(1, page - 1)
              )
            }
            className="rounded-lg border border-[#e5e5e5] px-4 py-2 disabled:opacity-40"
          >
            ‹
          </button>

          {pageNumbers.map((page) => (
            <button
              key={page}
              type="button"
              onClick={() => setCurrentPage(page)}
              className={`rounded-lg px-4 py-2 ${
                currentPage === page
                  ? "bg-[#004b3f] text-white"
                  : "border border-[#e5e5e5] bg-white"
              }`}
            >
              {page}
            </button>
          ))}

          <button
            type="button"
            disabled={currentPage === totalPages}
            onClick={() =>
              setCurrentPage((page) =>
                Math.min(totalPages, page + 1)
              )
            }
            className="rounded-lg border border-[#e5e5e5] px-4 py-2 disabled:opacity-40"
          >
            ›
          </button>
        </div>

        <div className="flex items-center gap-3">
          <span>
            {isDari
              ? "تعداد در هر صفحه:"
              : "Rows per page:"}
          </span>

          <select
            value={pageSize}
            onChange={(event) =>
              setPageSize(Number(event.target.value))
            }
            className="rounded-lg border border-[#e5e5e5] bg-white px-4 py-2"
          >
            <option value={10}>10</option>
            <option value={25}>25</option>
            <option value={50}>50</option>
          </select>
        </div>
      </div>
    </div>
  );
}