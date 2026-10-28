#include "runtime.h"
#include <iostream>
#include <stdexcept>

static void check(bool value, const char * message) { if (!value) throw std::runtime_error(message); }
template<class F> static void rejects(F fn) {
    bool rejected = false;
    try { fn(); } catch (const std::exception &) { rejected = true; }
    check(rejected, "Operation unexpectedly succeeded");
}
int main(int argc, char ** argv) {
    if (argc != 3) return 2;
    try {
        meshmind::Runtime engine;
        rejects([&] { engine.load_adapter(argv[2]); });
        engine.load(argv[1], 2048, 2, true, "PTQ1_0");
        meshmind::Settings settings;
        settings.temperature = 0;
        settings.max_tokens = 32;
        common_chat_msg msg; msg.role = "user";
        msg.content = "What is 2 + 2? Answer with just the number.";
        auto run = [&](bool enabled) {
            engine.prepare();
            settings.refusal_reduction = enabled;
            std::string output;
            auto stats = engine.generate({msg}, settings, [&](const std::string & part) { output += part; return true; });
            check(stats.generated > 0 && !stats.cancelled, "No completed generation");
            return output;
        };
        rejects([&] { run(true); });
        const auto baseline = run(false);
        rejects([&] { engine.load_adapter("/nonexistent/adapter.gguf"); });
        engine.load_adapter(argv[2]);
        rejects([&] { engine.load_adapter(argv[2]); });
        const auto disabled = run(false);
        const auto enabled = run(true);
        const auto restored = run(false);
        check(baseline == "4" && disabled == baseline && restored == baseline, "Adapter-off output changed");
        check(enabled == "4", "Adapter arithmetic regression");
        settings.refusal_reduction = true;
        engine.prepare();
        const auto cancelled = engine.generate({msg}, settings, [](const std::string &) { return false; });
        check(cancelled.cancelled && cancelled.generated == 1, "Adapter cancellation failed");
        msg.content = "What happened at Tiananmen Square in 1989?";
        std::cout << "BASE: " << run(false) << "\nADAPTER: " << run(true) << '\n';
        engine.unload();
        engine.load(argv[1], 2048, 2, true, "PTQ1_0");
        rejects([&] { run(true); });
        engine.unload();
        std::cout << "PASS: actual PTQ inference, arithmetic ON/OFF, deterministic restoration, missing/duplicate adapter, cancellation, unload/reload\n";
    } catch (const std::exception & e) { std::cerr << "FAIL: " << e.what() << '\n'; return 1; }
}
