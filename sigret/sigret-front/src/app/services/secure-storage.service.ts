import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SecureStorageService {
  private readonly ENCRYPTION_KEY = 'SIGRET_SECURE_KEY_2025';

  // Método para almacenar datos de forma segura
  setSecureItem(key: string, value: string): void {
    try {
      const encrypted = this.encryptSync(value);
      sessionStorage.setItem(key, encrypted);
    } catch (error) {
      console.error('Error storing secure item:', error);
    }
  }

  // Método para obtener datos de forma segura
  getSecureItem(key: string): string | null {
    try {
      const encrypted = sessionStorage.getItem(key);
      return encrypted ? this.decryptSync(encrypted) : null;
    } catch (error) {
      console.error('Error retrieving secure item:', error);
      return null;
    }
  }

  // Método para eliminar datos
  removeSecureItem(key: string): void {
    sessionStorage.removeItem(key);
  }

  // Método para limpiar todos los datos seguros
  clearSecureStorage(): void {
    const keysToRemove: string[] = [];
    
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i);
      if (key && (key.startsWith('auth_') || key.startsWith('user_'))) {
        keysToRemove.push(key);
      }
    }
    
    keysToRemove.forEach(key => sessionStorage.removeItem(key));
  }

  // Cifrado síncrono (versión básica para uso inmediato)
  private encryptSync(text: string): string {
    return this.encryptBasic(text);
  }

  private decryptSync(encryptedText: string): string {
    return this.decryptBasic(encryptedText);
  }

  // Cifrado asíncrono mejorado usando Web Crypto API si está disponible
  private async encryptAsync(text: string): Promise<string> {
    try {
      // Si Web Crypto API está disponible, usar AES
      if (window.crypto && window.crypto.subtle) {
        return await this.encryptWithWebCrypto(text);
      } else {
        // Fallback a cifrado básico
        return this.encryptBasic(text);
      }
    } catch (error) {
      console.error('Encryption error:', error);
      return this.encryptBasic(text);
    }
  }

  private async decryptAsync(encryptedText: string): Promise<string> {
    try {
      // Si Web Crypto API está disponible, usar AES
      if (window.crypto && window.crypto.subtle) {
        return await this.decryptWithWebCrypto(encryptedText);
      } else {
        // Fallback a descifrado básico
        return this.decryptBasic(encryptedText);
      }
    } catch (error) {
      console.error('Decryption error:', error);
      return this.decryptBasic(encryptedText);
    }
  }

  // Cifrado básico (fallback)
  private encryptBasic(text: string): string {
    let encrypted = '';
    for (let i = 0; i < text.length; i++) {
      const textChar = text.charCodeAt(i);
      const keyChar = this.ENCRYPTION_KEY.charCodeAt(i % this.ENCRYPTION_KEY.length);
      encrypted += String.fromCharCode(textChar ^ keyChar);
    }
    return btoa(encrypted);
  }

  private decryptBasic(encryptedText: string): string {
    try {
      const encrypted = atob(encryptedText);
      let decrypted = '';
      for (let i = 0; i < encrypted.length; i++) {
        const encryptedChar = encrypted.charCodeAt(i);
        const keyChar = this.ENCRYPTION_KEY.charCodeAt(i % this.ENCRYPTION_KEY.length);
        decrypted += String.fromCharCode(encryptedChar ^ keyChar);
      }
      return decrypted;
    } catch (error) {
      return '';
    }
  }

  // Cifrado con Web Crypto API (más seguro)
  private async encryptWithWebCrypto(text: string): Promise<string> {
    const encoder = new TextEncoder();
    const data = encoder.encode(text);
    
    const key = await this.getCryptoKey();
    const iv = window.crypto.getRandomValues(new Uint8Array(12));
    
    const encrypted = await window.crypto.subtle.encrypt(
      { name: 'AES-GCM', iv: iv },
      key,
      data
    );
    
    const encryptedArray = new Uint8Array(encrypted);
    const combined = new Uint8Array(iv.length + encryptedArray.length);
    combined.set(iv);
    combined.set(encryptedArray, iv.length);
    
    return btoa(String.fromCharCode(...combined));
  }

  private async decryptWithWebCrypto(encryptedText: string): Promise<string> {
    const combined = new Uint8Array(
      atob(encryptedText)
        .split('')
        .map(char => char.charCodeAt(0))
    );
    
    const iv = combined.slice(0, 12);
    const encrypted = combined.slice(12);
    
    const key = await this.getCryptoKey();
    
    const decrypted = await window.crypto.subtle.decrypt(
      { name: 'AES-GCM', iv: iv },
      key,
      encrypted
    );
    
    const decoder = new TextDecoder();
    return decoder.decode(decrypted);
  }

  private async getCryptoKey(): Promise<CryptoKey> {
    const encoder = new TextEncoder();
    const keyData = encoder.encode(this.ENCRYPTION_KEY);
    
    return await window.crypto.subtle.importKey(
      'raw',
      keyData,
      { name: 'AES-GCM' },
      false,
      ['encrypt', 'decrypt']
    );
  }
}
