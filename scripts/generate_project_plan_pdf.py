from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.cidfonts import UnicodeCIDFont
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


pdfmetrics.registerFont(UnicodeCIDFont("STSong-Light"))


TITLE = "智慧实验室管理平台项目计划书"
OUTPUT = Path(__file__).resolve().parents[1] / "智慧实验室管理平台项目计划书.pdf"


styles = getSampleStyleSheet()
styles.add(
    ParagraphStyle(
        name="CnTitle",
        parent=styles["Title"],
        fontName="STSong-Light",
        fontSize=22,
        leading=28,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#153A63"),
        spaceAfter=14,
    )
)
styles.add(
    ParagraphStyle(
        name="CnMeta",
        parent=styles["Normal"],
        fontName="STSong-Light",
        fontSize=10.5,
        leading=16,
        alignment=TA_CENTER,
        textColor=colors.HexColor("#555555"),
        spaceAfter=8,
    )
)
styles.add(
    ParagraphStyle(
        name="CnHeading",
        parent=styles["Heading2"],
        fontName="STSong-Light",
        fontSize=14,
        leading=20,
        textColor=colors.HexColor("#153A63"),
        spaceBefore=10,
        spaceAfter=6,
    )
)
styles.add(
    ParagraphStyle(
        name="CnBody",
        parent=styles["BodyText"],
        fontName="STSong-Light",
        fontSize=11,
        leading=19,
        alignment=TA_JUSTIFY,
        firstLineIndent=22,
        textColor=colors.HexColor("#222222"),
        spaceAfter=6,
    )
)


SECTIONS = [
    (
        "一、项目背景",
        "随着高校实验教学、科研创新和实验室开放管理需求不断提升，传统依赖纸质登记、人工审批和分散台账的实验室管理方式，已经难以满足现代化管理要求。实验室预约冲突频发、耗材账实不符、设备状态不可视、异常事件反馈滞后、资料管理分散等问题，直接影响实验教学效率与资源利用水平。在此背景下，建设一套融合预约审批、库存管理、设备接入、告警监测和智能辅助能力的智慧实验室平台，具有明显的现实价值与推广意义。",
    ),
    (
        "二、项目目标",
        "本项目旨在建设一套面向高校实验室场景的数字化综合管理平台，实现实验室资源、人员、耗材、设备与业务流程的统一管理。系统将重点解决实验室预约审批效率低、耗材管理不透明、设备数据无法实时联动、管理过程缺乏追踪与预警等问题。项目建设完成后，应形成“预约申请、审批处理、库存变更、设备监测、异常告警、资料归档、辅助查询”七位一体的业务闭环，并为后续智能化升级提供稳定底座。",
    ),
    (
        "三、产品定位",
        "本项目定位为高校与科研场景下的智慧实验室运营管理平台。它不仅是一个基础管理系统，更是一套具备实时感知、过程追踪、数据沉淀和扩展能力的平台型产品。平台面向实验室管理员、教师、学生等多类角色，通过统一入口完成预约、领用、审核、查询、监控和配置管理，并逐步向“实时化、智能化、平台化、可运维化”方向演进。",
    ),
    (
        "十、预期成果",
        "项目预期形成一套可运行的智慧实验室管理平台，具备预约、耗材、设备、告警、文件和智能助手等核心功能；同步沉淀项目文档、数据库脚本和演示材料；并为后续接入更多设备、扩展更多业务模块、增强智能决策能力提供持续演进空间。",
    ),
    (
        "十一、项目愿景",
        "本项目的长期愿景是从“实验室管理工具”升级为“实验室数字运营平台”。在完成基础业务闭环后，平台可继续向设备标准接入、库存自动换算、告警智能分析、知识资料检索、运营报告生成和多端协同使用等方向延伸，逐步形成兼具管理效率、数据价值与展示能力的智慧实验室平台体系。",
    ),
]


def make_table(rows, col_widths):
    table = Table(rows, colWidths=col_widths, repeatRows=1)
    table.setStyle(
        TableStyle(
            [
                ("FONTNAME", (0, 0), (-1, -1), "STSong-Light"),
                ("FONTSIZE", (0, 0), (-1, -1), 9.5),
                ("LEADING", (0, 0), (-1, -1), 14),
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#153A63")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("GRID", (0, 0), (-1, -1), 0.6, colors.HexColor("#B8C7D9")),
                ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F5F8FC")]),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                ("TOPPADDING", (0, 0), (-1, -1), 6),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    return table


def build_story():
    story = [
        Spacer(1, 18 * mm),
        Paragraph(TITLE, styles["CnTitle"]),
        Paragraph("项目名称：智衡实验云枢", styles["CnMeta"]),
        Paragraph("项目性质：高校智慧实验室综合管理平台", styles["CnMeta"]),
        Paragraph("编制日期：2026年5月31日", styles["CnMeta"]),
        Spacer(1, 8 * mm),
    ]

    for heading, body in SECTIONS[:3]:
        story.append(Paragraph(heading, styles["CnHeading"]))
        story.append(Paragraph(body, styles["CnBody"]))

    story.append(Paragraph("四、用户群体分析", styles["CnHeading"]))
    story.append(
        make_table(
            [
                ["用户角色", "核心诉求", "主要使用功能"],
                ["实验室管理员", "统一调度资源、掌握库存状态、处理异常事件", "预约审批、耗材审核、设备绑定、告警处理、用户管理、统计看板"],
                ["实验教师", "保障实验安排顺畅、掌握实验资源使用情况", "实验室预约查看、实验资料管理、资源协调"],
                ["学生", "快速完成预约与申领，查看进度与结果", "实验室预约、耗材申请、个人记录查询"],
                ["运维或项目维护人员", "保证平台稳定运行与配置可控", "配置管理、文件中心、设备接入、状态排查"],
            ],
            [28 * mm, 58 * mm, 82 * mm],
        )
    )
    story.append(Spacer(1, 4 * mm))

    story.append(Paragraph("五、核心建设内容", styles["CnHeading"]))
    for text in [
        "1. 实验室预约管理。系统支持实验室预约申请、预约记录展示、状态审核与结果反馈，能够减少预约冲突并提高实验场地使用效率。",
        "2. 耗材库存管理。系统支持耗材入库、库存展示、领用申请、审核扣减和库存变更日志记录，实现耗材全生命周期追踪。",
        "3. 设备接入与状态监测。平台支持设备注册、设备绑定、最近上报值展示、在线状态识别及基础监控展示，为物联网实验场景提供数据接入入口。",
        "4. 异常告警管理。系统针对库存偏低、传感器异常等情况生成告警，支持告警列表查询与人工确认，提升实验室安全和管理响应效率。",
        "5. 文件与资料中心。平台支持实验文档、实验指导书、管理附件等文件上传与统一管理，可作为实验室资料沉淀与共享入口。",
        "6. 智能助手能力。系统预留并实现了基础智能助手模块，支持库存摘要、待办任务、设备态势与配置状态查询，为后续 Agent 工具化升级提供基础。",
    ]:
        story.append(Paragraph(text, styles["CnBody"]))

    story.append(Paragraph("六、技术路线", styles["CnHeading"]))
    story.append(
        make_table(
            [
                ["层次", "技术方案", "说明"],
                ["前端", "Vue 3 + Vite + Element Plus", "构建统一管理界面与交互体验"],
                ["后端", "Spring Boot 3 + MyBatis-Plus", "承载业务逻辑、接口服务与数据访问"],
                ["数据库", "MySQL", "存储用户、预约、耗材、设备与告警等核心业务数据"],
                ["缓存", "Redis", "支持首页统计缓存、会话扩展与后续状态管理能力"],
                ["对象存储", "MinIO", "支持文件上传与资料中心建设"],
                ["配置中心", "Nacos", "支持配置集中管理与环境扩展"],
                ["实时通信", "SSE", "支持库存、预约、设备与告警变更的前端实时刷新"],
                ["IoT 接入", "HTTP / MQTT 预留", "适配实验室设备上报与物联网联动场景"],
            ],
            [24 * mm, 60 * mm, 84 * mm],
        )
    )
    story.append(Spacer(1, 4 * mm))

    story.append(Paragraph("七、实施计划", styles["CnHeading"]))
    story.append(
        make_table(
            [
                ["阶段", "时间安排", "主要任务", "阶段成果"],
                ["第一阶段：需求梳理与方案设计", "第1-2周", "明确业务需求、用户角色、功能边界与技术路线", "完成需求分析与系统方案设计"],
                ["第二阶段：基础功能开发", "第3-5周", "完成登录鉴权、用户管理、预约管理、耗材管理", "形成基础业务闭环原型"],
                ["第三阶段：设备与实时能力接入", "第6-7周", "完成设备绑定、状态展示、SSE 推送、告警中心", "实现基础实时化能力"],
                ["第四阶段：文件与智能模块建设", "第8周", "完成文件中心、智能助手入口、配置中心展示", "平台功能进一步完善"],
                ["第五阶段：联调测试与优化", "第9-10周", "完成功能联调、异常修复、体验优化、文档整理", "输出可演示、可答辩、可交付版本"],
            ],
            [44 * mm, 20 * mm, 62 * mm, 50 * mm],
        )
    )
    story.append(Spacer(1, 4 * mm))

    story.append(Paragraph("八、项目组织与分工建议", styles["CnHeading"]))
    story.append(
        make_table(
            [
                ["角色", "主要职责"],
                ["项目负责人", "统筹项目进度、把控需求边界、组织汇报与验收"],
                ["前端开发", "负责页面开发、交互实现、接口联调与可视化展示"],
                ["后端开发", "负责接口设计、业务实现、权限控制与数据管理"],
                ["物联网接入负责人", "负责设备数据接入、协议联调与传感器测试"],
                ["测试与文档负责人", "负责测试验证、问题整理、文档输出与成果归档"],
            ],
            [42 * mm, 134 * mm],
        )
    )
    story.append(Spacer(1, 4 * mm))

    story.append(Paragraph("九、项目风险分析", styles["CnHeading"]))
    story.append(
        make_table(
            [
                ["风险项", "风险说明", "应对策略"],
                ["需求变化风险", "中后期需求扩展过快，影响开发节奏", "先固化核心范围，按阶段迭代扩展"],
                ["设备接入风险", "传感器数据不稳定，可能影响库存准确性", "增加绑定规则、异常过滤和日志追踪"],
                ["权限与安全风险", "管理员操作和设备上报存在安全边界问题", "强化后端鉴权、敏感操作校验和配置隔离"],
                ["进度风险", "多模块并行开发可能导致联调滞后", "提前约定接口边界，按周同步进展"],
                ["展示效果风险", "若页面过于偏后台表格，项目亮点不足", "强化首页看板、监控态势与智能助手展示"],
            ],
            [30 * mm, 64 * mm, 82 * mm],
        )
    )
    story.append(Spacer(1, 4 * mm))

    for heading, body in SECTIONS[3:]:
        story.append(Paragraph(heading, styles["CnHeading"]))
        story.append(Paragraph(body, styles["CnBody"]))

    story.append(Paragraph("十二、结论", styles["CnHeading"]))
    story.append(
        Paragraph(
            "综合来看，智慧实验室管理平台项目具有明确的现实需求、清晰的功能边界和较强的扩展潜力。项目既能够解决高校实验室日常管理中的预约、库存、设备与告警问题，也具备向实时化、智能化与平台化升级的可持续基础。通过分阶段推进建设，本项目能够形成兼顾落地价值、展示效果与后续演进空间的高质量成果。",
            styles["CnBody"],
        )
    )
    return story


def main():
    doc = SimpleDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=18 * mm,
        rightMargin=18 * mm,
        topMargin=16 * mm,
        bottomMargin=16 * mm,
        title=TITLE,
        author="OpenAI Codex",
    )
    doc.build(build_story())
    print(OUTPUT)


if __name__ == "__main__":
    main()
