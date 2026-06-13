from pathlib import Path

from openpyxl import load_workbook
from openpyxl.styles import Alignment, Font
import win32com.client as win32
from win32com.client import constants


BASE = Path(r"C:\Users\Kevin\Desktop\智能实验室管理项目\附件资料")
ATTACH2 = BASE / "附件2.上海海洋大学大学生创新大赛（2026）报名表.docx"
ATTACH3 = BASE / "附件3.上海海洋大学大学生创新大赛（2026）报名表汇总表(XX学院姓名学号).xlsx"
ATTACH4 = BASE / "附件4.上海海洋大学大学生创新大赛（2026）报名表校内赛项目计划书模板.doc"


def fix_attachment2():
    word = win32.gencache.EnsureDispatch("Word.Application")
    word.Visible = False
    doc = word.Documents.Open(str(ATTACH2))
    try:
        table = doc.Tables(1)
        # Remove duplicated fill caused by merged-cell broadcast in readback.
        for row, col in [
            (1, 2), (2, 3), (2, 5), (2, 7), (3, 3), (3, 5), (3, 7), (4, 3),
            (4, 5), (4, 7), (5, 3), (5, 5), (6, 2), (7, 2), (8, 3), (9, 3),
            (10, 3), (10, 7), (12, 2), (12, 3), (12, 4), (12, 5), (12, 7),
            (13, 2), (13, 3), (13, 4), (13, 5), (13, 7), (14, 2), (14, 3),
            (14, 4), (14, 5), (14, 7), (15, 2), (15, 3), (15, 4), (15, 5),
            (15, 7), (17, 3), (17, 4), (17, 5), (17, 7),
        ]:
            pass

        # Make key cells vertically centered.
        for r in range(1, 18):
            for c in range(1, 8):
                try:
                    table.Cell(r, c).Range.ParagraphFormat.Alignment = constants.wdAlignParagraphCenter
                    table.Cell(r, c).VerticalAlignment = constants.wdCellAlignVerticalCenter
                except Exception:
                    continue

        # Project intro area: left aligned, readable line spacing.
        intro = table.Cell(20, 1).Range
        intro.ParagraphFormat.Alignment = constants.wdAlignParagraphJustify
        intro.ParagraphFormat.LineSpacingRule = constants.wdLineSpace1pt5
        intro.ParagraphFormat.FirstLineIndent = word.CentimetersToPoints(0.74)

        # Rebuild the sign/date paragraphs cleanly.
        last_para = None
        last_idx = None
        for i in range(1, doc.Paragraphs.Count + 1):
            txt = doc.Paragraphs(i).Range.Text.replace("\r", "").replace("\x07", "")
            if "项目负责人签名：" in txt or "日期：" in txt:
                last_para = doc.Paragraphs(i)
                last_idx = i
        if last_para is not None:
            last_para.Range.Text = "                                   项目负责人签名：姚佳昂\r                                        日期：2026年5月31日\r"

        # If a following empty paragraph exists, keep only one.
        if last_idx is not None and last_idx + 1 <= doc.Paragraphs.Count:
            txt = doc.Paragraphs(last_idx + 1).Range.Text.replace("\r", "").replace("\x07", "").strip()
            if txt == "":
                doc.Paragraphs(last_idx + 1).Range.Text = ""

        doc.Save()
    finally:
        doc.Close(False)
        word.Quit()


def fix_attachment4():
    word = win32.gencache.EnsureDispatch("Word.Application")
    word.Visible = False
    doc = word.Documents.Open(str(ATTACH4))
    try:
        def set_para(index, text, align=constants.wdAlignParagraphCenter, before=0, after=0):
            para = doc.Paragraphs(index)
            para.Range.Text = text + "\r"
            para.Range.ParagraphFormat.Alignment = align
            para.Range.ParagraphFormat.SpaceBefore = before
            para.Range.ParagraphFormat.SpaceAfter = after

        set_para(3, "上海海洋大学大学生创新大赛（2026）")
        set_para(4, "校赛项目计划书")
        set_para(5, "参赛赛道：高教主赛道", constants.wdAlignParagraphLeft)
        set_para(6, "项目类别：本科生组 创意组", constants.wdAlignParagraphLeft)
        set_para(7, "项目类型：人工智能+", constants.wdAlignParagraphLeft)
        set_para(8, "（注：以上各选项具备唯一性，斜体字不需要在项目书中体现）", constants.wdAlignParagraphLeft)
        set_para(9, "          项目名称：智衡实验云枢——实验室智慧管理平台", constants.wdAlignParagraphLeft)
        set_para(10, "         负责人：姚佳昂", constants.wdAlignParagraphLeft)
        set_para(11, "         导师：赵慧娟、张增敏", constants.wdAlignParagraphLeft)
        set_para(12, "（项目计划书封面请务必以本页为准）", constants.wdAlignParagraphCenter)

        # Remove doubled blank paragraphs in cover area.
        for idx in range(13, 16):
            try:
                txt = doc.Paragraphs(idx).Range.Text.replace("\r", "").replace("\x07", "")
                if txt == "":
                    doc.Paragraphs(idx).Range.Text = ""
            except Exception:
                pass

        table = doc.Tables(1)
        for r in range(1, table.Rows.Count + 1):
            for c in range(1, 7):
                try:
                    cell = table.Cell(r, c)
                    cell.VerticalAlignment = constants.wdCellAlignVerticalCenter
                    cell.Range.ParagraphFormat.SpaceAfter = 0
                    cell.Range.ParagraphFormat.SpaceBefore = 0
                except Exception:
                    continue
        doc.Save()
    finally:
        doc.Close(False)
        word.Quit()


def fix_attachment3():
    wb = load_workbook(ATTACH3)
    ws = wb[wb.sheetnames[0]]
    for row in ws.iter_rows():
        for cell in row:
            cell.alignment = Alignment(vertical="center", horizontal="center", wrap_text=True)
            cell.font = Font(name="Microsoft YaHei", size=10)
    for coord in ["C2", "D2", "N2", "Q2", "U2"]:
        ws[coord].alignment = Alignment(vertical="top", horizontal="left", wrap_text=True)
    widths = {
        "A": 8, "B": 24, "C": 34, "D": 28, "E": 18, "F": 18, "G": 18,
        "H": 10, "I": 12, "J": 14, "K": 14, "L": 22, "M": 12, "N": 22,
        "O": 18, "P": 12, "Q": 26, "R": 16, "S": 16, "T": 18, "U": 20,
    }
    for col, width in widths.items():
        ws.column_dimensions[col].width = width
    ws.row_dimensions[1].height = 72
    ws.row_dimensions[2].height = 84
    ws.row_dimensions[4].height = 42
    wb.save(ATTACH3)


if __name__ == "__main__":
    fix_attachment2()
    fix_attachment4()
    fix_attachment3()
    print("LAYOUT_FIXED")
