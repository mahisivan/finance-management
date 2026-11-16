import debtTxBanner from '../resources/images/banners/debt_tx_banner.png'
import cardTxBanner from '../resources/images/banners/card_tx_banner.png'
import loanTxBanner from '../resources/images/banners/loan_tx_banner.png'
import salaryTxBanner from '../resources/images/banners/salary_tx_banner.png'

const banners = {
  'debt_tx_banner.png': debtTxBanner,
  'card_tx_banner.png': cardTxBanner,
  'loan_tx_banner.png': loanTxBanner,
  'salary_tx_banner.png': salaryTxBanner,
}

const loadBannerData = async (bannerUrl) => {
  const response = await fetch(bannerUrl)
  const blob = await response.blob()
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

export class PdfGenerator {
  static async download(columns, rows, filename, title, bannerName) {
    const [{ jsPDF }, { default: autoTable }] = await Promise.all([
      import('jspdf'),
      import('jspdf-autotable'),
    ])
    const document = new jsPDF({ orientation: columns.length > 7 ? 'landscape' : 'portrait' })
    let startY = 16
    const bannerUrl = banners[bannerName]
    if (bannerUrl) {
      try {
        const bannerData = await loadBannerData(bannerUrl)
        const pageWidth = document.internal.pageSize.getWidth()
        const bannerWidth = pageWidth - 28
        const bannerHeight = 18
        document.addImage(bannerData, 'PNG', 14, 8, bannerWidth, bannerHeight)
        startY = 32
      } catch (error) {
        console.warn('Unable to add PDF banner:', error)
      }
    }
    document.setFontSize(15)
    document.text(title, 14, startY)
    autoTable(document, {
      startY: startY + 7,
      head: [columns.map(column => column.label)],
      body: rows.map(row => columns.map(column => {
        const value = column.value ? column.value(row) : row[column.key]
        return value == null ? '' : String(value)
      })),
      styles: { fontSize: columns.length > 7 ? 7 : 9 },
    })
    document.save(`${filename}.pdf`)
  }
}
