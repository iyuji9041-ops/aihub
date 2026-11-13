package com.foss.aihub.utils

import android.content.Context
import android.webkit.WebSettings

const val USER_AGENT_DESKTOP =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36"
const val USER_AGENT_MOBILE =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.7632.121 Mobile Safari/537.36"
const val GITHUB_USER_NAME = "SilentCoderHere"
const val GITHUB_REPO_NAME = "aihub"
const val SUPPORT_EMAIL = "silentcoder@tutamail.com"
const val CLOUD_BASE_URL = "https://silentcoderhere.github.io/aihub-config-data/"
const val AI_SERVICES_FILE = "ais.json"
const val DOMAINS_FILE = "domains.txt"

private const val FALLBACK_MAJOR_VERSION = "131"
private const val FALLBACK_FULL_VERSION = "131.0.6778.135"

fun extractChromiumVersion(context: Context): Pair<String, String> {
    val defaultUa = try {
        WebSettings.getDefaultUserAgent(context)
    } catch (_: Exception) {
        ""
    }
    val match = Regex("Chrome/((\\d+)(?:\\.[0-9]+)+)").find(defaultUa)
    return if (match != null) {
        val fullVersion = match.groupValues[1]
        val majorVersion = match.groupValues[2]
        Pair(majorVersion, fullVersion)
    } else {
        Pair(FALLBACK_MAJOR_VERSION, FALLBACK_FULL_VERSION)
    }
}

fun getCleanUserAgent(context: Context, isDesktop: Boolean): String {
    val (_, fullVersion) = extractChromiumVersion(context)
    if (isDesktop) {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$fullVersion Safari/537.36"
    }

    val defaultUa = try {
        WebSettings.getDefaultUserAgent(context)
    } catch (_: Exception) {
        ""
    }

    return if (defaultUa.isNotBlank()) {
        defaultUa
            .replace("; wv", "")
            .replace(";wv", "")
            .replace(Regex(";\\s*wv\\b"), "")
            .replace(Regex("\\bwv\\b"), "")
            .replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    } else {
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$fullVersion Mobile Safari/537.36"
    }
}

/**
 * Universal JavaScript snippet to inject a prompt and optionally submit it across
 * modern AI interfaces (ChatGPT, Claude, Gemini, DeepSeek, Copilot, Perplexity, etc.).
 * Handles both standard <textarea>/<input> and contenteditable elements (React/ProseMirror/Slate).
 */
fun buildPromptInjectionJs(promptText: String, autoSubmit: Boolean): String {
    val quotedText = org.json.JSONObject.quote(promptText)
    return """
        (function() {
            try {
                const text = $quotedText;
                const autoSend = $autoSubmit;

                function findChatInput() {
                    const selectors = [
                        '#prompt-textarea',
                        'textarea[data-id="root"]',
                        'div[contenteditable="true"][role="textbox"]',
                        'div[contenteditable="true"]',
                        'textarea',
                        'input[type="text"]'
                    ];
                    for (const sel of selectors) {
                        const elements = Array.from(document.querySelectorAll(sel));
                        for (const el of elements) {
                            const rect = el.getBoundingClientRect();
                            const style = window.getComputedStyle(el);
                            if (rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none') {
                                return el;
                            }
                        }
                    }
                    return null;
                }

                const input = findChatInput();
                if (!input) return false;

                input.focus();

                if (input.isContentEditable) {
                    document.execCommand('selectAll', false, null);
                    const inserted = document.execCommand('insertText', false, text);
                    if (!inserted) {
                        input.textContent = text;
                        input.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: text }));
                    }
                } else {
                    const proto = window.HTMLTextAreaElement.prototype;
                    const desc = Object.getOwnPropertyDescriptor(proto, 'value');
                    if (desc && desc.set) {
                        desc.set.call(input, text);
                    } else {
                        input.value = text;
                    }
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                }

                if (autoSend) {
                    setTimeout(() => {
                        const btnSelectors = [
                            'button[data-testid="send-button"]',
                            'button[aria-label*="Send" i]',
                            'button[aria-label*="Submit" i]',
                            'button[id*="send" i]',
                            'button.send-button',
                            'button[type="submit"]'
                        ];
                        for (const sel of btnSelectors) {
                            const btn = document.querySelector(sel);
                            if (btn && !btn.disabled) {
                                btn.click();
                                return;
                            }
                        }
                        input.dispatchEvent(new KeyboardEvent('keydown', {
                            bubbles: true,
                            cancelable: true,
                            key: 'Enter',
                            code: 'Enter',
                            keyCode: 13,
                            which: 13
                        }));
                    }, 300);
                }
                return true;
            } catch (e) {
                console.error("AI Hub Prompt Injection Error:", e);
                return false;
            }
        })();
    """.trimIndent()
}
