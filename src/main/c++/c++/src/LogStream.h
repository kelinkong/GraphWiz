//
// Created by kelin on 24-4-23.
// 日志处理
//

#ifndef LOGSTREAM_H
#define LOGSTREAM_H
#include <qstring.h>
#include <streambuf>
#include <QTextBrowser>

#include "Logger.h"

class LogStream final : public std::streambuf {
public:
    explicit LogStream(QTextBrowser* text_browser) : text_browser(text_browser) {}

protected:
    std::streamsize xsputn(const char *p, const std::streamsize n) override {
        QString str(p);
        if (str.endsWith('\n')) {
            str.chop(1);
        }
        text_browser->append(str);
        return n;
    }

    int_type overflow(const int_type ch) override {
        if (ch != traits_type::eof()) {
            const char z = static_cast<char>(ch);
            const QString str = QString::fromLatin1(&z, 1);
            text_browser->append(str);
        }
        return ch;
    }

private:
    QTextBrowser* text_browser;
};


class GurobiCallback final : public GRBCallback {
public:
    explicit GurobiCallback(QTextBrowser* logBrowser) : text_browser(logBrowser) {}

protected:
    void callback() override {
        if (where == GRB_CB_MESSAGE) {
            // Get the log message
            std::string message = getStringInfo(GRB_CB_MSG_STRING);
            // Send the log message to QTextBrowser
            text_browser->append(QString::fromStdString(message));
        }
    }
private:
    QTextBrowser* text_browser;
};
#endif //LOGSTREAM_H
