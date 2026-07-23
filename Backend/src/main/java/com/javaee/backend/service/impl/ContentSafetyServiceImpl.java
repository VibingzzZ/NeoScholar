package com.javaee.backend.service.impl;

import com.javaee.backend.service.ContentSafetyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 内容安全审核服务实现
 * <p>
 * 对用户输入进行多层次安全审核，包括：
 * <ul>
 *   <li>Prompt 注入攻击检测</li>
 *   <li>有害内容过滤（涉政、色情、暴力、仇恨言论等）</li>
 *   <li>教育平台特定内容审核</li>
 * </ul>
 */
@Slf4j
@Service
public class ContentSafetyServiceImpl implements ContentSafetyService {

    // ======================== Prompt 注入检测 ========================

    /** 试图覆盖/绕过系统指令的模式 */
    private static final Pattern[] INJECTION_PATTERNS = {
            // 英文注入
            Pattern.compile("ignore\\s+(all\\s+)?(previous|above|prior|before)\\s+(instructions?|prompts?|messages?|rules?)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("forget\\s+(all\\s+)?(previous|above|prior|before)\\s+(instructions?|prompts?|messages?)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now\\s+(a\\s+)?(different|new|another)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("pretend\\s+(you\\s+are|to\\s+be)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("act\\s+as\\s+(a\\s+)?(different|another|new)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(from\\s+now\\s+on|starting\\s+now)\\s+you\\s+(are|will\\s+be)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(system\\s*|)\\[(system\\s*|)(prompt|instruction|message)\\]",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\|?im_start\\|?>|<\\|?im_end\\|?>"),
            // 中文注入
            Pattern.compile("忽略(所有|之前|上面|前面|此前)的?(指令|提示|规则|要求|对话|内容)"),
            Pattern.compile("忘记(所有|之前|上面|前面)的?(指令|提示|规则|对话)"),
            Pattern.compile("你现在(是|扮演|作为)(一个|新的|另一个)"),
            Pattern.compile("假装(你是|自己是)"),
            Pattern.compile("从现在开始(你是|你要)"),
            Pattern.compile("(系统|角色)(指令|提示|设定)"),
            // DAN / 越狱
            Pattern.compile("\\bDAN\\b.*\\b(do\\s+anything\\s+now|mode|jailbreak)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bjailbreak\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("开发者模式|越狱模式|无限制模式"),
    };

    // ======================== 有害内容关键词 ========================

    /** 严重违规 — 涉政敏感词 */
    private static final Pattern[] POLITICAL_PATTERNS = {
            Pattern.compile("法轮功|falungong|falun\\s*dafa", Pattern.CASE_INSENSITIVE),
            Pattern.compile("六四|天安门(事件|屠杀|镇压)|tiananmen\\s*square", Pattern.CASE_INSENSITIVE),
            Pattern.compile("台独|藏独|疆独|港独|东突"),
            Pattern.compile("(分裂|颠覆)(国家|政权)"),
            Pattern.compile("(反党|反共|反华|反中)(言论|组织|势力)?"),
    };

    /** 色情/低俗内容 */
    private static final Pattern[] PORNOGRAPHY_PATTERNS = {
            Pattern.compile("(色情|淫秽|黄色)(小说|图片|视频|网站|内容)?"),
            Pattern.compile("(裸体|裸露|脱衣|色诱)"),
            Pattern.compile("(做爱|性交|口交|肛交|自慰|手淫)"),
            Pattern.compile("\\b(porn|sex\\s*(video|chat|tape)|nude|naked|xxx)\\b", Pattern.CASE_INSENSITIVE),
    };

    /** 暴力/恐怖内容 */
    private static final Pattern[] VIOLENCE_PATTERNS = {
            Pattern.compile("(杀人|谋杀|暗杀|刺杀)(方法|技巧|教程|指南)?"),
            Pattern.compile("(制造|制作)(炸弹|炸药|武器|枪支|毒药)(方法|教程|指南)?"),
            Pattern.compile("(恐怖|极端)(主义|组织|分子|袭击)"),
            Pattern.compile("\\b(terroris[mt]|bomb\\s*making|how\\s*to\\s*kill)\\b", Pattern.CASE_INSENSITIVE),
    };

    /** 仇恨言论/歧视 */
    private static final Pattern[] HATE_SPEECH_PATTERNS = {
            Pattern.compile("(种族|地域|性别|宗教|民族)(歧视|仇恨|攻击)"),
            Pattern.compile("(支那|黑鬼|白皮猪|绿绿|棒子|倭寇|阿三)"),
            Pattern.compile("\\b(nigger|chink|faggot|retard)\\b", Pattern.CASE_INSENSITIVE),
    };

    /** 赌博/毒品/违法 */
    private static final Pattern[] ILLEGAL_PATTERNS = {
            Pattern.compile("(赌博|赌场|赌球|六合彩)(网站|平台|网址|推荐)?"),
            Pattern.compile("(毒品|吸毒|大麻|海洛因|冰毒|摇头丸)(购买|制作|吸食)?"),
            Pattern.compile("(贩卖|走私|洗钱|诈骗)(方法|教程|技巧)?"),
    };

    /** 自残/自杀 */
    private static final Pattern[] SELF_HARM_PATTERNS = {
            Pattern.compile("(自杀|自残|自伤|割腕|跳楼|上吊)(方法|教程|群|小组)?"),
            Pattern.compile("\\b(suicide\\s*(method|pact|guide)|self\\s*harm)\\b", Pattern.CASE_INSENSITIVE),
    };

    // ======================== 审核方法 ========================

    @Override
    public boolean isSafe(String content) {
        if (content == null || content.isBlank()) {
            return true;
        }

        // 检测 Prompt 注入攻击
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(content).find()) {
                log.warn("[内容安全] 检测到 Prompt 注入攻击");
                return false;
            }
        }

        // 检测有害内容
        return !matchesAnyHarmfulPattern(content);
    }

    // ======================== 私有方法 ========================

    /**
     * 检测有害内容（涉政、色情、暴力、仇恨、违法、自残等）
     */
    private boolean matchesAnyHarmfulPattern(String content) {
        Pattern[][] allPatterns = {
                POLITICAL_PATTERNS,
                PORNOGRAPHY_PATTERNS,
                VIOLENCE_PATTERNS,
                HATE_SPEECH_PATTERNS,
                ILLEGAL_PATTERNS,
                SELF_HARM_PATTERNS
        };

        for (Pattern[] patterns : allPatterns) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(content).find()) {
                    log.warn("[内容安全] 检测到不当内容");
                    return true;
                }
            }
        }

        return false;
    }
}
