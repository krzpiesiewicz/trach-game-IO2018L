
#ifndef TRACH_HEALTHBARUI_H
#define TRACH_HEALTHBARUI_H


#include <QtWidgets/QWidget>
#include <QtWidgets/QLabel>

class HealthBarUI : public QWidget
{
private:

    QLabel* textLabel;

    const std::string getLabelText(int healthValue)
    {
        std::string result;
        result += "health : ";
        result += std::to_string(healthValue);
        return result;
    }

public:
    HealthBarUI(QWidget* parent, int healthValue)
    : QWidget(parent)
    {
        textLabel = new QLabel(this);
        setHealthValue(healthValue);

    }

    void setHealthValue(int newValue)
    {
        textLabel->setText(QString::fromStdString(getLabelText(newValue)));
    }
};


#endif //TRACH_HEALTHBARUI_H
