import Checker


def print_all(text):
    words = list(Checker.correct_core(text))
    print('\n' + text)

    best_match = 'Fixed: '
    for word in words:
        if type(word) == list:
            best_match += word[0]
        else:
            best_match += word
    print(best_match + '\n')

    for word in words:
        print(word)


# '我已经等猴多时了.' Wǒ yǐjīng děng hóu duōshíliǎo. Я долго ждал обезьяну.
def main():
    text = '我已经等猴多时了.'
    print_all(text)


if __name__ == '__main__':
    main()
