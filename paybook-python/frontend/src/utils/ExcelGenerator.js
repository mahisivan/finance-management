export class ExcelGenerator {
  static async download(columns, rows, filename) {
    const XLSX = await import('xlsx')
    const records = rows.map(row => Object.fromEntries(columns.map(column => [column.label, column.value ? column.value(row) : row[column.key]])))
    const worksheet = XLSX.utils.json_to_sheet(records)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Results')
    XLSX.writeFile(workbook, `${filename}.xlsx`)
  }
}
