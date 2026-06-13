$ErrorActionPreference = "Stop"

$base = "C:\Users\Kevin\Desktop\智能实验室管理项目\附件资料"
$attachment2 = Join-Path $base "附件2.上海海洋大学大学生创新大赛（2026）报名表.docx"
$attachment3 = Join-Path $base "附件3.上海海洋大学大学生创新大赛（2026）报名表汇总表(XX学院姓名学号).xlsx"
$attachment4 = Join-Path $base "附件4.上海海洋大学大学生创新大赛（2026）报名表校内赛项目计划书模板.doc"

$projectName = "智衡实验云枢——实验室智慧管理平台"
$leaderName = "姚佳昂"
$leaderId = "2551226"
$leaderCollege = "信息学院"
$leaderMajor = "计算机科学与技术"
$leaderPhone = "18837236586"
$leaderEmail = "3478917829@qq.com"
$leaderDegree = "本科"
$leaderEnrollYear = "2025"
$leaderGraduateYear = "2029"
$secondContactName = "雷璐灵"
$secondContactPhone = "13701640968"
$trackText = "高教主赛道 / 本科生组 / 创意组"
$projectTypeText = "人工智能+"
$companyName = "无"
$companyAddress = "无"
$companyRegister = "无"
$companyLegal = "无"
$teamCount = "5"
$teachersText = "赵慧娟（信息学院，讲师，算法设计）；张增敏（工程学院，教师，工程数据库/机器人工程/人工智能/程序设计）"

$intro800 = @"
“智衡实验云枢”聚焦高校实验室预约冲突、耗材账实不符、设备状态不可视和安全响应滞后等问题，建设一套集实验室预约、耗材库存、设备接入、异常告警、文件中心和智能助手于一体的智慧实验室管理平台。项目以高校实验教学和科研实验室为主要应用场景，围绕实验室预约审批、耗材申领审核、库存变更追踪、设备绑定监测与异常预警等核心环节，形成从申请、审批、执行、记录到分析的完整闭环。平台采用 Spring Boot 3 与 Vue 3 前后端分离架构，结合 MySQL、Redis、MinIO、Nacos 等组件构建稳定底座，并引入 RFID、重力传感器与 AI 识别能力，推动实验室资源管理从人工登记向实时感知、从经验处理向数据驱动升级。项目产品定位为面向高校的智慧实验室运营管理平台，既服务管理员、教师和学生的日常业务，又具备向多设备接入、智能分析和运营辅助决策持续扩展的能力。项目当前以校内实验室应用需求为切入点，后续可拓展至科研院所、实训基地和企业研发实验室等场景。商业模式上，前期以校内示范应用和系统落地为主，后续可探索平台部署服务、功能模块扩展和软硬件一体化升级路径。团队成员覆盖前后端开发、数据库设计、物联网硬件、人工智能与项目统筹，具备跨学科协同优势。
"@

$summary100to200 = "本项目面向高校实验室数字化管理场景，围绕预约审批、耗材库存、设备监测、文件中心与智能助手构建统一平台。系统结合 Spring Boot、Vue、MySQL 与物联网感知能力，实现实验资源调度、库存追踪、设备状态展示和异常告警联动，提升实验室运行效率、透明度与安全性，并具备向智能运营平台持续扩展的基础。"
$innovation100 = "创新点在于将实验室预约、耗材管理、设备接入和 AI/IoT 感知能力统一到同一平台，实现库存与设备状态实时联动；难点在于多角色流程协同、传感器数据与业务规则映射，以及系统实时性与可扩展性的平衡。"

$teamMembers = @(
    @{ Name = "雷璐灵"; Id = "2550212"; Role = "项目负责人/统筹"; Phone = "13701640968"; Major = "测控"; Degree = "本科" },
    @{ Name = "鄺健豪"; Id = "2550330"; Role = "人工智能/视觉算法"; Phone = "13166399078"; Major = "制药"; Degree = "本科" },
    @{ Name = "许家怿"; Id = "2534315"; Role = "物联网/硬件开发"; Phone = "18121070218"; Major = "机器人"; Degree = "本科" },
    @{ Name = "欧阳中昊"; Id = "2551217"; Role = "调试/企划宣发"; Phone = "18099908237"; Major = "机器人"; Degree = "本科" }
)

$guides = @(
    @{ WorkNo = "待补充"; Name = "赵慧娟"; College = "信息学院"; Title = "讲师"; Duty = "指导教师" },
    @{ WorkNo = "待补充"; Name = "张增敏"; College = "工程学院"; Title = "教师"; Duty = "指导教师" }
)

$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    $doc = $word.Documents.Open($attachment2)
    $table = $doc.Tables.Item(1)

    $table.Cell(1,2).Range.Text = $projectName
    $table.Cell(2,3).Range.Text = $leaderId
    $table.Cell(2,5).Range.Text = $leaderEnrollYear
    $table.Cell(2,7).Range.Text = $leaderCollege
    $table.Cell(3,3).Range.Text = $leaderName
    $table.Cell(3,5).Range.Text = $leaderGraduateYear
    $table.Cell(3,7).Range.Text = $leaderMajor
    $table.Cell(4,3).Range.Text = $leaderPhone
    $table.Cell(4,5).Range.Text = $leaderEmail
    $table.Cell(4,7).Range.Text = $leaderDegree
    $table.Cell(5,3).Range.Text = $secondContactName
    $table.Cell(5,5).Range.Text = $secondContactPhone
    $table.Cell(6,2).Range.Text = $trackText
    $table.Cell(7,2).Range.Text = $projectTypeText
    $table.Cell(8,3).Range.Text = $companyName
    $table.Cell(9,3).Range.Text = $companyAddress
    $table.Cell(10,3).Range.Text = $companyRegister
    $table.Cell(10,5).Range.Text = $companyLegal

    for ($i = 0; $i -lt $teamMembers.Count; $i++) {
        $row = 12 + $i
        $member = $teamMembers[$i]
        $table.Cell($row,2).Range.Text = "$($member.Name)/$($member.Id)"
        $table.Cell($row,3).Range.Text = $member.Role
        $table.Cell($row,4).Range.Text = $member.Phone
        $table.Cell($row,5).Range.Text = $member.Major
        $table.Cell($row,6).Range.Text = $member.Degree
    }

    $guideRow = 17
    $guideText = ($guides | ForEach-Object { "$($_.Name) / $($_.WorkNo) / $($_.College) / $($_.Title)" }) -join "`r"
    $table.Cell($guideRow,2).Range.Text = ($guides | ForEach-Object { $_.WorkNo }) -join "；"
    $table.Cell($guideRow,3).Range.Text = ($guides | ForEach-Object { $_.Name }) -join "；"
    $table.Cell($guideRow,4).Range.Text = ($guides | ForEach-Object { $_.College }) -join "；"
    $table.Cell($guideRow,5).Range.Text = ($guides | ForEach-Object { $_.Title }) -join "；"
    $table.Cell($guideRow,6).Range.Text = "指导教师"

    $table.Cell(20,1).Range.Text = $intro800

    $doc.Paragraphs.Item($doc.Paragraphs.Count - 1).Range.Text = "                                   项目负责人签名：$leaderName"
    $doc.Paragraphs.Item($doc.Paragraphs.Count).Range.Text = "                                        日期：2026年5月31日"
    $doc.Save()
    $doc.Close()

    $doc4 = $word.Documents.Open($attachment4)
    $doc4.Content.Find.Execute("项目名称：", $false, $false, $false, $false, $false, $true, 1, $false, "项目名称：$projectName", 2) | Out-Null
    $doc4.Content.Find.Execute("负责人：", $false, $false, $false, $false, $false, $true, 1, $false, "负责人：$leaderName", 2) | Out-Null
    $doc4.Content.Find.Execute("导师：", $false, $false, $false, $false, $false, $true, 1, $false, "导师：赵慧娟、张增敏", 2) | Out-Null

    $tables4 = @($doc4.Tables)
    $t4 = $tables4[0]
    $t4.Cell(2,1).Range.Text = $intro800
    $t4.Cell(4,2).Range.Text = "$leaderName / $leaderId"
    $t4.Cell(4,4).Range.Text = $leaderEnrollYear
    $t4.Cell(4,6).Range.Text = $leaderGraduateYear
    $t4.Cell(5,2).Range.Text = $leaderMajor
    $t4.Cell(5,4).Range.Text = $leaderDegree
    $t4.Cell(6,2).Range.Text = $leaderPhone
    $t4.Cell(6,4).Range.Text = $leaderEmail
    $t4.Cell(8,1).Range.Text = "团队人数（3-15人）：$teamCount 人"

    $planMembers = @(
        @{ Name = $leaderName; Id = $leaderId; Role = "前后端开发/数据库"; Major = $leaderMajor; Degree = $leaderDegree }
        @{ Name = "雷璐灵"; Id = "2550212"; Role = "项目统筹/测控"; Major = "测控"; Degree = "本科" }
        @{ Name = "鄺健豪"; Id = "2550330"; Role = "人工智能/视觉算法"; Major = "制药"; Degree = "本科" }
        @{ Name = "许家怿"; Id = "2534315"; Role = "物联网/硬件开发"; Major = "机器人"; Degree = "本科" }
        @{ Name = "欧阳中昊"; Id = "2551217"; Role = "调试/企划宣发"; Major = "机器人"; Degree = "本科" }
    )

    for ($i = 0; $i -lt $planMembers.Count; $i++) {
        $row = 10 + $i
        $m = $planMembers[$i]
        $t4.Cell($row,2).Range.Text = "$($m.Name) / $($m.Id)"
        $t4.Cell($row,3).Range.Text = $m.Role
        $t4.Cell($row,4).Range.Text = $m.Major
        $t4.Cell($row,5).Range.Text = $m.Degree
    }

    $t4.Cell(16,2).Range.Text = "赵慧娟；张增敏"
    $t4.Cell(16,4).Range.Text = "信息学院；工程学院"
    $t4.Cell(17,2).Range.Text = "讲师；教师"
    $t4.Cell(17,4).Range.Text = "算法设计；工程数据库/机器人工程/人工智能/程序设计"
    $doc4.Save()
    $doc4.Close()
}
finally {
    $word.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
}

$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
try {
    $wb = $excel.Workbooks.Open($attachment3)
    $ws = $wb.Worksheets.Item(1)

    $ws.Cells.Item(2,2).Value2 = $projectName
    $ws.Cells.Item(2,3).Value2 = $summary100to200
    $ws.Cells.Item(2,4).Value2 = $innovation100
    $ws.Cells.Item(2,5).Value2 = "主赛道本科生组"
    $ws.Cells.Item(2,6).Value2 = "创意组"
    $ws.Cells.Item(2,7).Value2 = "人工智能+"
    $ws.Cells.Item(2,8).Value2 = $leaderName
    $ws.Cells.Item(2,9).Value2 = $leaderId
    $ws.Cells.Item(2,10).Value2 = $leaderCollege
    $ws.Cells.Item(2,11).Value2 = $leaderPhone
    $ws.Cells.Item(2,12).Value2 = $leaderEmail
    $ws.Cells.Item(2,13).Value2 = $teamCount
    $ws.Cells.Item(2,14).Value2 = "雷璐灵/2550212,`n鄺健豪/2550330,`n许家怿/2534315,`n欧阳中昊/2551217"
    $ws.Cells.Item(2,15).Value2 = "赵慧娟/待补充，张增敏/待补充"
    $ws.Cells.Item(2,16).Value2 = "讲师，教师"
    $ws.Cells.Item(2,17).Value2 = "算法设计；工程数据库、机器人工程、人工智能、程序设计"
    $ws.Cells.Item(2,18).Value2 = "15692165685，15618063223"
    $ws.Cells.Item(2,19).Value2 = "待补充"
    $ws.Cells.Item(2,20).Value2 = "信息学院，工程学院"
    $ws.Cells.Item(2,21).Value2 = "立项大创项目，需同步完成官网和学院报名"
    $wb.Save()
    $wb.Close($true)
}
finally {
    $excel.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($excel) | Out-Null
}
